package vn.edu.iuh.fit.bookstorebackend.service.Impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.iuh.fit.bookstorebackend.common.PromotionStatus;
import vn.edu.iuh.fit.bookstorebackend.dto.request.CreatePromotionRequest;
import vn.edu.iuh.fit.bookstorebackend.dto.response.PromotionResponse;
import vn.edu.iuh.fit.bookstorebackend.exception.IdInvalidException;
import vn.edu.iuh.fit.bookstorebackend.model.Promotion;
import vn.edu.iuh.fit.bookstorebackend.model.User;
import vn.edu.iuh.fit.bookstorebackend.mapper.PromotionMapper;
import vn.edu.iuh.fit.bookstorebackend.repository.PromotionRepository;
import vn.edu.iuh.fit.bookstorebackend.repository.UserRepository;
import vn.edu.iuh.fit.bookstorebackend.service.NotificationService;
import vn.edu.iuh.fit.bookstorebackend.service.PromotionService;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PromotionServiceImpl implements PromotionService {

    private final PromotionRepository promotionRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final PromotionMapper promotionMapper;

    @Override
    @Transactional
    public PromotionResponse createPromotion(CreatePromotionRequest request) throws IdInvalidException {
        validateRequest(request);
        validateCreatePromotionRequest(request);
        validateCodeNotExists(request.getCode());
        
        User currentUser = getCurrentUser();
        Promotion promotion = createPromotionFromRequest(request, currentUser);
        
        Promotion savedPromotion = promotionRepository.save(promotion);
        return promotionMapper.toPromotionResponse(savedPromotion);
    }
    
    private void validateRequest(CreatePromotionRequest request) throws IdInvalidException {
        if (request == null) {
            throw new IdInvalidException("CreatePromotionRequest cannot be null");
        }
    }
    
    private void validateCodeNotExists(String code) {
        if (promotionRepository.existsByCode(code)) {
            throw new RuntimeException("Promotion code already exists: " + code);
        }
    }
    
    private Promotion createPromotionFromRequest(CreatePromotionRequest request, User currentUser) {
        Promotion promotion = promotionMapper.toPromotion(request);
        promotion.setPriceOrderActive(request.getPriceOrderActive() != null 
                ? request.getPriceOrderActive() : 0.0);
        promotion.setStatus(PromotionStatus.PENDING);
        promotion.setIsActive(true);
        promotion.setCreatedBy(currentUser);
        promotion.setApprovedBy(null);
        return promotion;
    }

    @Override
    @Transactional
    public PromotionResponse approvePromotion(Long promotionId) throws IdInvalidException {
        validatePromotionId(promotionId);
        Promotion promotion = findPromotionById(promotionId);
        validatePromotionStatus(promotion, PromotionStatus.PENDING, "Only PENDING promotions can be approved");
        
        User currentUser = getCurrentUser();
        approvePromotion(promotion, currentUser);
        
        Promotion updatedPromotion = promotionRepository.save(promotion);
        notifyActiveCustomers(promotionId);
        
        return promotionMapper.toPromotionResponse(updatedPromotion);
    }
    
    private void validatePromotionId(Long promotionId) throws IdInvalidException {
        if (promotionId == null || promotionId <= 0) {
            throw new IdInvalidException("Promotion identifier is invalid: " + promotionId);
        }
    }
    
    private Promotion findPromotionById(Long promotionId) {
        return promotionRepository.findById(promotionId)
                .orElseThrow(() -> new RuntimeException("Promotion not found with identifier: " + promotionId));
    }
    
    private void validatePromotionStatus(Promotion promotion, PromotionStatus expectedStatus, String errorMessage) {
        if (promotion.getStatus() != expectedStatus) {
            throw new RuntimeException(errorMessage);
        }
    }
    
    private void approvePromotion(Promotion promotion, User currentUser) {
        promotion.setStatus(PromotionStatus.ACTIVE);
        promotion.setIsActive(true);
        promotion.setApprovedBy(currentUser);
    }

    @Override
    @Transactional
    public PromotionResponse deactivatePromotion(Long promotionId) throws IdInvalidException {
        validatePromotionId(promotionId);
        Promotion promotion = findPromotionById(promotionId);
        
        deactivatePromotion(promotion);
        Promotion updatedPromotion = promotionRepository.save(promotion);
        
        return promotionMapper.toPromotionResponse(updatedPromotion);
    }
    
    private void deactivatePromotion(Promotion promotion) {
        promotion.setStatus(PromotionStatus.REJECTED);
        promotion.setIsActive(false);
    }

    @Override
    @Transactional
    public PromotionResponse pausePromotion(Long promotionId) throws IdInvalidException {
        validatePromotionId(promotionId);
        Promotion promotion = findPromotionById(promotionId);
        validatePromotionStatus(promotion, PromotionStatus.ACTIVE, "Only ACTIVE promotions can be paused");
        
        pausePromotion(promotion);
        Promotion updatedPromotion = promotionRepository.save(promotion);
        notifyPromotionPaused(promotionId);
        
        return promotionMapper.toPromotionResponse(updatedPromotion);
    }
    
    private void pausePromotion(Promotion promotion) {
        promotion.setStatus(PromotionStatus.PAUSED);
    }

    @Override
    @Transactional
    public PromotionResponse resumePromotion(Long promotionId) throws IdInvalidException {
        validatePromotionId(promotionId);
        Promotion promotion = findPromotionById(promotionId);
        validatePromotionStatus(promotion, PromotionStatus.PAUSED, "Only PAUSED promotions can be resumed");
        
        resumePromotion(promotion);
        Promotion updatedPromotion = promotionRepository.save(promotion);
        notifyPromotionResumed(promotionId);
        
        return promotionMapper.toPromotionResponse(updatedPromotion);
    }
    
    private void resumePromotion(Promotion promotion) {
        promotion.setStatus(PromotionStatus.ACTIVE);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PromotionResponse> searchPromotions(String name, String code, PromotionStatus status, Boolean isActive) {
        List<Promotion> promotions = promotionRepository.searchPromotions(name, code, status, isActive);
        return mapToPromotionResponseList(promotions);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PromotionResponse> getAllPromotions() {
        List<Promotion> promotions = promotionRepository.findAll();
        return mapToPromotionResponseList(promotions);
    }

    @Override
    @Transactional(readOnly = true)
    public PromotionResponse getPromotionById(Long promotionId) throws IdInvalidException {
        validatePromotionId(promotionId);
        Promotion promotion = findPromotionById(promotionId);
        return promotionMapper.toPromotionResponse(promotion);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean validatePromotionCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            return false;
        }

        Promotion promotion = promotionRepository.findByCode(code.trim()).orElse(null);
        if (promotion == null) {
            return false;
        }

        return isPromotionValid(promotion);
    }
    
    private boolean isPromotionValid(Promotion promotion) {
        LocalDate today = LocalDate.now();
        return promotion.getIsActive() 
                && promotion.getStatus() == PromotionStatus.ACTIVE
                && !today.isBefore(promotion.getStartDate())
                && !today.isAfter(promotion.getEndDate())
                && promotion.getQuantity() > 0;
    }

    @Override
    @Transactional
    public void notifyPromotionPaused(Long promotionId) throws IdInvalidException {
        Promotion promotion = findPromotionById(promotionId);
        List<User> activeUsers = getActiveUsers();
        
        String title = "Khuyến mãi tạm dừng";
        String content = buildPausedNotificationContent(promotion);
        
        sendNotificationsToUsers(activeUsers, title, content);
    }
    
    private String buildPausedNotificationContent(Promotion promotion) {
        return String.format("Khuyến mãi '%s' (Mã: %s) đã tạm dừng. Vui lòng theo dõi để biết thêm thông tin.",
                promotion.getName(), promotion.getCode());
    }

    @Override
    @Transactional
    public void notifyActiveCustomers(Long promotionId) throws IdInvalidException {
        Promotion promotion = findPromotionById(promotionId);
        List<User> activeUsers = getActiveUsers();
        
        String title = "Khuyến mãi mới";
        String content = buildActivatedNotificationContent(promotion);
        
        sendNotificationsToUsers(activeUsers, title, content);
    }
    
    private String buildActivatedNotificationContent(Promotion promotion) {
        return String.format("Khuyến mãi '%s' (Mã: %s) đã được kích hoạt! Giảm giá %.0f%%. Áp dụng từ %s đến %s.",
                promotion.getName(), promotion.getCode(), promotion.getDiscountPercent(),
                promotion.getStartDate(), promotion.getEndDate());
    }

    @Override
    @Transactional
    public void notifyPromotionResumed(Long promotionId) throws IdInvalidException {
        Promotion promotion = findPromotionById(promotionId);
        List<User> activeUsers = getActiveUsers();
        
        String title = "Khuyến mãi tiếp tục";
        String content = buildResumedNotificationContent(promotion);
        
        sendNotificationsToUsers(activeUsers, title, content);
    }
    
    private String buildResumedNotificationContent(Promotion promotion) {
        return String.format("Khuyến mãi '%s' (Mã: %s) đã được tiếp tục! Giảm giá %.0f%%. Áp dụng từ %s đến %s.",
                promotion.getName(), promotion.getCode(), promotion.getDiscountPercent(),
                promotion.getStartDate(), promotion.getEndDate());
    }
    
    private List<User> getActiveUsers() {
        return userRepository.findAll().stream()
                .filter(User::isActive)
                .collect(Collectors.toList());
    }
    
    private void sendNotificationsToUsers(List<User> users, String title, String content) {
        for (User user : users) {
            notificationService.createNotification(null, user, title, content);
        }
    }

    private User getCurrentUser() throws IdInvalidException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        if (auth == null) {
            throw new RuntimeException("Authentication context is null. Please login first.");
        }
        
        if (!auth.isAuthenticated()) {
            throw new RuntimeException("User is not authenticated. Please login first.");
        }
        
        if (auth instanceof AnonymousAuthenticationToken) {
            throw new RuntimeException("User is not authenticated. Please login first.");
        }

        String email = auth.getName();
        if (email == null || email.trim().isEmpty()) {
            throw new RuntimeException("User email is not found in authentication context.");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

        if (!user.isActive()) {
            throw new RuntimeException("User account is inactive. Please contact administrator.");
        }

        return user;
    }

    private void validateCreatePromotionRequest(CreatePromotionRequest request) {
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new RuntimeException("Promotion name cannot be null or empty");
        }
        if (request.getCode() == null || request.getCode().trim().isEmpty()) {
            throw new RuntimeException("Promotion code cannot be null or empty");
        }
        if (request.getDiscountPercent() == null || request.getDiscountPercent() <= 0 || request.getDiscountPercent() > 100) {
            throw new RuntimeException("Discount percent must be between 1 and 100");
        }
        if (request.getStartDate() == null) {
            throw new RuntimeException("Start date cannot be null");
        }
        if (request.getEndDate() == null) {
            throw new RuntimeException("End date cannot be null");
        }
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new RuntimeException("End date cannot be before start date");
        }
        if (request.getQuantity() == null || request.getQuantity() <= 0) {
            throw new RuntimeException("Quantity must be greater than 0");
        }
    }

    private List<PromotionResponse> mapToPromotionResponseList(List<Promotion> promotions) {
        return promotions.stream()
                .map(promotionMapper::toPromotionResponse)
                .collect(Collectors.toList());
    }
}
