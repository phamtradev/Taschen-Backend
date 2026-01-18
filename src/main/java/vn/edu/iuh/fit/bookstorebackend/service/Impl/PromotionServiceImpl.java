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

    @Override
    @Transactional
    public PromotionResponse createPromotion(CreatePromotionRequest request) throws IdInvalidException {
        if (request == null) {
            throw new IdInvalidException("CreatePromotionRequest cannot be null");
        }

        // Validate request
        validateCreatePromotionRequest(request);

        // Check if code already exists
        if (promotionRepository.existsByCode(request.getCode())) {
            throw new RuntimeException("Promotion code already exists: " + request.getCode());
        }

        // Get current user (creator)
        User currentUser = getCurrentUser();

        // Create promotion
        Promotion promotion = new Promotion();
        promotion.setName(request.getName());
        promotion.setCode(request.getCode());
        promotion.setDiscountPercent(request.getDiscountPercent());
        promotion.setStartDate(request.getStartDate());
        promotion.setEndDate(request.getEndDate());
        promotion.setQuantity(request.getQuantity());
        promotion.setPriceOrderActive(request.getPriceOrderActive() != null ? request.getPriceOrderActive() : 0.0);
        promotion.setStatus(PromotionStatus.PENDING); // Mặc định chờ duyệt
        promotion.setIsActive(true);
        promotion.setCreatedBy(currentUser);
        promotion.setApprovedBy(null); // Chưa được duyệt

        Promotion savedPromotion = promotionRepository.save(promotion);
        return convertToPromotionResponse(savedPromotion);
    }

    @Override
    @Transactional
    public PromotionResponse approvePromotion(Long promotionId) throws IdInvalidException {
        if (promotionId == null || promotionId <= 0) {
            throw new IdInvalidException("Promotion identifier is invalid: " + promotionId);
        }

        Promotion promotion = promotionRepository.findById(promotionId)
                .orElseThrow(() -> new RuntimeException("Promotion not found with identifier: " + promotionId));

        if (promotion.getStatus() != PromotionStatus.PENDING) {
            throw new RuntimeException("Only PENDING promotions can be approved");
        }

        User currentUser = getCurrentUser();
        promotion.setStatus(PromotionStatus.ACTIVE);
        promotion.setIsActive(true);
        promotion.setApprovedBy(currentUser);

        Promotion updatedPromotion = promotionRepository.save(promotion);
        
        // Thông báo cho khách hàng active
        notifyActiveCustomers(promotionId);
        
        return convertToPromotionResponse(updatedPromotion);
    }

    @Override
    @Transactional
    public PromotionResponse deactivatePromotion(Long promotionId) throws IdInvalidException {
        if (promotionId == null || promotionId <= 0) {
            throw new IdInvalidException("Promotion identifier is invalid: " + promotionId);
        }

        Promotion promotion = promotionRepository.findById(promotionId)
                .orElseThrow(() -> new RuntimeException("Promotion not found with identifier: " + promotionId));

        promotion.setStatus(PromotionStatus.REJECTED);
        promotion.setIsActive(false);

        Promotion updatedPromotion = promotionRepository.save(promotion);
        return convertToPromotionResponse(updatedPromotion);
    }

    @Override
    @Transactional
    public PromotionResponse pausePromotion(Long promotionId) throws IdInvalidException {
        if (promotionId == null || promotionId <= 0) {
            throw new IdInvalidException("Promotion identifier is invalid: " + promotionId);
        }

        Promotion promotion = promotionRepository.findById(promotionId)
                .orElseThrow(() -> new RuntimeException("Promotion not found with identifier: " + promotionId));

        if (promotion.getStatus() != PromotionStatus.ACTIVE) {
            throw new RuntimeException("Only ACTIVE promotions can be paused");
        }

        promotion.setStatus(PromotionStatus.PAUSED);

        Promotion updatedPromotion = promotionRepository.save(promotion);
        
        // Thông báo promotion bị tạm dừng
        notifyPromotionPaused(promotionId);
        
        return convertToPromotionResponse(updatedPromotion);
    }

    @Override
    @Transactional
    public PromotionResponse resumePromotion(Long promotionId) throws IdInvalidException {
        if (promotionId == null || promotionId <= 0) {
            throw new IdInvalidException("Promotion identifier is invalid: " + promotionId);
        }

        Promotion promotion = promotionRepository.findById(promotionId)
                .orElseThrow(() -> new RuntimeException("Promotion not found with identifier: " + promotionId));

        if (promotion.getStatus() != PromotionStatus.PAUSED) {
            throw new RuntimeException("Only PAUSED promotions can be resumed");
        }

        promotion.setStatus(PromotionStatus.ACTIVE);

        Promotion updatedPromotion = promotionRepository.save(promotion);
        
        // Thông báo promotion được tiếp tục
        notifyPromotionResumed(promotionId);
        
        return convertToPromotionResponse(updatedPromotion);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PromotionResponse> searchPromotions(String name, String code, PromotionStatus status, Boolean isActive) {
        List<Promotion> promotions = promotionRepository.searchPromotions(name, code, status, isActive);
        return promotions.stream()
                .map(this::convertToPromotionResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PromotionResponse> getAllPromotions() {
        List<Promotion> promotions = promotionRepository.findAll();
        return promotions.stream()
                .map(this::convertToPromotionResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PromotionResponse getPromotionById(Long promotionId) throws IdInvalidException {
        if (promotionId == null || promotionId <= 0) {
            throw new IdInvalidException("Promotion identifier is invalid: " + promotionId);
        }

        Promotion promotion = promotionRepository.findById(promotionId)
                .orElseThrow(() -> new RuntimeException("Promotion not found with identifier: " + promotionId));

        return convertToPromotionResponse(promotion);
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

        // Kiểm tra promotion có active và trong thời gian hiệu lực không
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
        Promotion promotion = promotionRepository.findById(promotionId)
                .orElseThrow(() -> new RuntimeException("Promotion not found with identifier: " + promotionId));

        // Lấy tất cả user active
        List<User> activeUsers = userRepository.findAll().stream()
                .filter(User::isActive)
                .collect(Collectors.toList());

        String title = "Khuyến mãi tạm dừng";
        String content = String.format("Khuyến mãi '%s' (Mã: %s) đã tạm dừng. Vui lòng theo dõi để biết thêm thông tin.",
                promotion.getName(), promotion.getCode());

        // Gửi thông báo cho tất cả user active
        for (User user : activeUsers) {
            notificationService.createNotification(null, user, title, content);
        }
    }

    @Override
    @Transactional
    public void notifyActiveCustomers(Long promotionId) throws IdInvalidException {
        Promotion promotion = promotionRepository.findById(promotionId)
                .orElseThrow(() -> new RuntimeException("Promotion not found with identifier: " + promotionId));

        // Lấy tất cả user active
        List<User> activeUsers = userRepository.findAll().stream()
                .filter(User::isActive)
                .collect(Collectors.toList());

        String title = "Khuyến mãi mới";
        String content = String.format("Khuyến mãi '%s' (Mã: %s) đã được kích hoạt! Giảm giá %.0f%%. Áp dụng từ %s đến %s.",
                promotion.getName(), promotion.getCode(), promotion.getDiscountPercent(),
                promotion.getStartDate(), promotion.getEndDate());

        // Gửi thông báo cho tất cả user active
        for (User user : activeUsers) {
            notificationService.createNotification(null, user, title, content);
        }
    }

    @Override
    @Transactional
    public void notifyPromotionResumed(Long promotionId) throws IdInvalidException {
        Promotion promotion = promotionRepository.findById(promotionId)
                .orElseThrow(() -> new RuntimeException("Promotion not found with identifier: " + promotionId));

        // Lấy tất cả user active
        List<User> activeUsers = userRepository.findAll().stream()
                .filter(User::isActive)
                .collect(Collectors.toList());

        String title = "Khuyến mãi tiếp tục";
        String content = String.format("Khuyến mãi '%s' (Mã: %s) đã được tiếp tục! Giảm giá %.0f%%. Áp dụng từ %s đến %s.",
                promotion.getName(), promotion.getCode(), promotion.getDiscountPercent(),
                promotion.getStartDate(), promotion.getEndDate());

        // Gửi thông báo cho tất cả user active
        for (User user : activeUsers) {
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

    private PromotionResponse convertToPromotionResponse(Promotion promotion) {
        PromotionResponse response = new PromotionResponse();
        response.setId(promotion.getId());
        response.setName(promotion.getName());
        response.setCode(promotion.getCode());
        response.setDiscountPercent(promotion.getDiscountPercent());
        response.setStartDate(promotion.getStartDate());
        response.setEndDate(promotion.getEndDate());
        response.setQuantity(promotion.getQuantity());
        response.setIsActive(promotion.getIsActive());
        response.setStatus(promotion.getStatus());
        response.setPriceOrderActive(promotion.getPriceOrderActive());

        if (promotion.getCreatedBy() != null) {
            response.setCreatedById(promotion.getCreatedBy().getId());
            response.setCreatedByName(getUserDisplayName(promotion.getCreatedBy()));
        }

        if (promotion.getApprovedBy() != null) {
            response.setApprovedById(promotion.getApprovedBy().getId());
            response.setApprovedByName(getUserDisplayName(promotion.getApprovedBy()));
        }

        return response;
    }

    private String getUserDisplayName(User user) {
        if (user.getFirstName() != null && user.getLastName() != null) {
            return user.getFirstName() + " " + user.getLastName();
        } else if (user.getFirstName() != null) {
            return user.getFirstName();
        } else if (user.getLastName() != null) {
            return user.getLastName();
        } else {
            return user.getEmail();
        }
    }
}
