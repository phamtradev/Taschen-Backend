package vn.edu.iuh.fit.bookstorebackend.order.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import vn.edu.iuh.fit.bookstorebackend.shared.dto.WsEvent;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.iuh.fit.bookstorebackend.shared.common.OrderStatus;
import vn.edu.iuh.fit.bookstorebackend.shared.common.ReturnRequestStatus;
import vn.edu.iuh.fit.bookstorebackend.order.dto.request.CreateReturnRequestRequest;
import vn.edu.iuh.fit.bookstorebackend.order.dto.request.ProcessReturnRequestRequest;
import vn.edu.iuh.fit.bookstorebackend.order.dto.response.ReturnRequestResponse;
import vn.edu.iuh.fit.bookstorebackend.shared.exception.IdInvalidException;
import vn.edu.iuh.fit.bookstorebackend.order.mapper.ReturnRequestMapper;
import vn.edu.iuh.fit.bookstorebackend.order.model.Order;
import vn.edu.iuh.fit.bookstorebackend.order.model.ReturnRequest;
import vn.edu.iuh.fit.bookstorebackend.user.model.User;
import vn.edu.iuh.fit.bookstorebackend.order.repository.OrderRepository;
import vn.edu.iuh.fit.bookstorebackend.order.repository.ReturnRequestRepository;
import vn.edu.iuh.fit.bookstorebackend.user.repository.UserRepository;
import vn.edu.iuh.fit.bookstorebackend.notification.service.NotificationService;
import vn.edu.iuh.fit.bookstorebackend.order.service.OrderService;
import vn.edu.iuh.fit.bookstorebackend.order.service.ReturnRequestService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReturnRequestServiceImpl implements ReturnRequestService {

    private final ReturnRequestRepository returnRequestRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final OrderService orderService;
    private final NotificationService notificationService;
    private final ReturnRequestMapper returnRequestMapper;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    @Transactional
    public ReturnRequestResponse createReturnRequest(CreateReturnRequestRequest request) throws IdInvalidException {
        User currentUser = getCurrentUser();
        Order order = findOrderById(request.getOrderId());
        validateOrderOwnership(order, currentUser);
        validateOrderStatusForReturn(order);
        validateReturnRequestNotExists(order.getId());

        ReturnRequest returnRequest = createReturnRequestFromRequest(request, order, currentUser);
        ReturnRequest savedReturnRequest = returnRequestRepository.save(returnRequest);
        messagingTemplate.convertAndSend("/topic/return-requests",
                new WsEvent("CREATED", "RETURN_REQUEST", savedReturnRequest.getId(), null));
        notificationService.notifyAllByRole("ADMIN", "Yêu cầu trả hàng #" + savedReturnRequest.getId() + " mới", "Khách hàng vừa gửi yêu cầu trả hàng cần xem xét");
        notificationService.notifyAllByRole("SELLER", "Yêu cầu trả hàng #" + savedReturnRequest.getId() + " mới", "Khách hàng vừa gửi yêu cầu trả hàng cần xem xét");

        return returnRequestMapper.toReturnRequestResponse(savedReturnRequest);
    }

    private Order findOrderById(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with identifier: " + orderId));
    }

    private void validateOrderOwnership(Order order, User currentUser) {
        if (!order.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Order does not belong to user");
        }
    }

    private void validateOrderStatusForReturn(Order order) {
        if (order.getStatus() != OrderStatus.COMPLETED) {
            throw new RuntimeException("Order must be COMPLETED to create return request. Current status: " + order.getStatus());
        }
    }

    private void validateReturnRequestNotExists(Long orderId) {
        if (returnRequestRepository.existsByOrder_Id(orderId)) {
            throw new RuntimeException("Return request already exists for this order");
        }
    }

    private ReturnRequest createReturnRequestFromRequest(CreateReturnRequestRequest request, Order order, User createdBy) {
        ReturnRequest returnRequest = new ReturnRequest();
        returnRequest.setOrder(order);
        returnRequest.setReason(request.getReason());
        returnRequest.setStatus(ReturnRequestStatus.PENDING);
        returnRequest.setCreatedBy(createdBy);
        returnRequest.setCreatedAt(LocalDateTime.now());
        return returnRequest;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReturnRequestResponse> getMyReturnRequests() {
        User currentUser = getCurrentUser();
        List<ReturnRequest> returnRequests = returnRequestRepository.findByCreatedBy_IdOrderByCreatedAtDesc(currentUser.getId());
        return mapToReturnRequestResponseList(returnRequests);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReturnRequestResponse> getAllReturnRequests() {
        List<ReturnRequest> returnRequests = returnRequestRepository.findAllByOrderByCreatedAtDesc();
        return mapToReturnRequestResponseList(returnRequests);
    }

    private List<ReturnRequestResponse> mapToReturnRequestResponseList(List<ReturnRequest> returnRequests) {
        return returnRequests.stream()
                .map(returnRequestMapper::toReturnRequestResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ReturnRequestResponse approveReturnRequest(Long returnRequestId, ProcessReturnRequestRequest request) throws IdInvalidException {
        validateReturnRequestId(returnRequestId);

        ReturnRequest returnRequest = findReturnRequestById(returnRequestId);
        validateReturnRequestStatusForProcessing(returnRequest);

        User currentUser = getCurrentUser();

        approveReturnRequest(returnRequest, currentUser, request.getResponseNote());
        updateOrderStatusToReturned(returnRequest.getOrder().getId());

        ReturnRequest savedReturnRequest = returnRequestRepository.save(returnRequest);
        messagingTemplate.convertAndSend("/topic/return-requests",
                new WsEvent("UPDATED", "RETURN_REQUEST", savedReturnRequest.getId(), null));
        notificationService.createNotification(null, savedReturnRequest.getCreatedBy(),
                "Yeu cau tra hang #" + savedReturnRequest.getId() + " duoc chap nhan",
                "Yeu cau tra hang cua ban da duoc chap nhan");
        return returnRequestMapper.toReturnRequestResponse(savedReturnRequest);
    }

    private void approveReturnRequest(ReturnRequest returnRequest, User processedBy, String responseNote) {
        returnRequest.setStatus(ReturnRequestStatus.APPROVED);
        returnRequest.setProcessedBy(processedBy);
        returnRequest.setProcessedAt(LocalDateTime.now());
        returnRequest.setResponseNote(responseNote);
    }

    private void updateOrderStatusToReturned(Long orderId) throws IdInvalidException {
        orderService.updateOrderStatus(orderId, OrderStatus.RETURNED);
    }

    @Override
    @Transactional
    public ReturnRequestResponse rejectReturnRequest(Long returnRequestId, ProcessReturnRequestRequest request) throws IdInvalidException {
        validateReturnRequestId(returnRequestId);

        ReturnRequest returnRequest = findReturnRequestById(returnRequestId);
        validateReturnRequestStatusForProcessing(returnRequest);

        User currentUser = getCurrentUser();

        rejectReturnRequest(returnRequest, currentUser, request.getResponseNote());

        ReturnRequest savedReturnRequest = returnRequestRepository.save(returnRequest);
        messagingTemplate.convertAndSend("/topic/return-requests",
                new WsEvent("UPDATED", "RETURN_REQUEST", savedReturnRequest.getId(), null));
        notificationService.createNotification(null, savedReturnRequest.getCreatedBy(),
                "Yeu cau tra hang #" + savedReturnRequest.getId() + " bi tu choi",
                "Yeu cau tra hang cua ban da bi tu choi");
        return returnRequestMapper.toReturnRequestResponse(savedReturnRequest);
    }

    private void rejectReturnRequest(ReturnRequest returnRequest, User processedBy, String responseNote) {
        returnRequest.setStatus(ReturnRequestStatus.REJECTED);
        returnRequest.setProcessedBy(processedBy);
        returnRequest.setProcessedAt(LocalDateTime.now());
        returnRequest.setResponseNote(responseNote);
    }

    private void validateReturnRequestId(Long returnRequestId) throws IdInvalidException {
        if (returnRequestId == null || returnRequestId <= 0) {
            throw new IdInvalidException("Return request id is invalid");
        }
    }

    private void validateReturnRequestStatusForProcessing(ReturnRequest returnRequest) {
        if (returnRequest.getStatus() != ReturnRequestStatus.PENDING) {
            throw new RuntimeException("Return request can only be processed when status is PENDING. Current status: " + returnRequest.getStatus());
        }
    }

    private ReturnRequest findReturnRequestById(Long returnRequestId) {
        return returnRequestRepository.findById(returnRequestId)
                .orElseThrow(() -> new RuntimeException("Return request not found"));
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        validateAuthentication(auth);

        String email = extractEmailFromAuth(auth);
        User user = findUserByEmail(email);
        validateUserIsActive(user);

        return user;
    }

    private void validateAuthentication(Authentication auth) {
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            throw new RuntimeException("User is not authenticated. Please login first.");
        }
    }

    private String extractEmailFromAuth(Authentication auth) {
        String email = auth.getName();
        if (email == null || email.trim().isEmpty()) {
            throw new RuntimeException("User email is not found in authentication context.");
        }
        return email;
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
    }

    private void validateUserIsActive(User user) {
        if (!user.isActive()) {
            throw new RuntimeException("User account is inactive. Please contact administrator.");
        }
    }
}
