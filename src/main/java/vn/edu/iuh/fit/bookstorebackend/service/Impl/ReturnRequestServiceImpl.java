package vn.edu.iuh.fit.bookstorebackend.service.Impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.iuh.fit.bookstorebackend.common.OrderStatus;
import vn.edu.iuh.fit.bookstorebackend.common.ReturnRequestStatus;
import vn.edu.iuh.fit.bookstorebackend.dto.request.CreateReturnRequestRequest;
import vn.edu.iuh.fit.bookstorebackend.dto.request.ProcessReturnRequestRequest;
import vn.edu.iuh.fit.bookstorebackend.dto.response.ReturnRequestResponse;
import vn.edu.iuh.fit.bookstorebackend.exception.IdInvalidException;
import vn.edu.iuh.fit.bookstorebackend.mapper.ReturnRequestMapper;
import vn.edu.iuh.fit.bookstorebackend.model.Order;
import vn.edu.iuh.fit.bookstorebackend.model.ReturnRequest;
import vn.edu.iuh.fit.bookstorebackend.model.User;
import vn.edu.iuh.fit.bookstorebackend.repository.OrderRepository;
import vn.edu.iuh.fit.bookstorebackend.repository.ReturnRequestRepository;
import vn.edu.iuh.fit.bookstorebackend.repository.UserRepository;
import vn.edu.iuh.fit.bookstorebackend.service.OrderService;
import vn.edu.iuh.fit.bookstorebackend.service.ReturnRequestService;

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
    private final ReturnRequestMapper returnRequestMapper;

    @Override
    @Transactional
    public ReturnRequestResponse createReturnRequest(CreateReturnRequestRequest request) throws IdInvalidException {
        validateCreateReturnRequestRequest(request);

        User currentUser = getCurrentUser();
        Order order = findOrderById(request.getOrderId());
        validateOrderOwnership(order, currentUser);
        validateOrderStatusForReturn(order);
        validateReturnRequestNotExists(order.getId());

        ReturnRequest returnRequest = createReturnRequestFromRequest(request, order, currentUser);
        ReturnRequest savedReturnRequest = returnRequestRepository.save(returnRequest);

        return returnRequestMapper.toReturnRequestResponse(savedReturnRequest);
    }

    private void validateCreateReturnRequestRequest(CreateReturnRequestRequest request) throws IdInvalidException {
        if (request == null) {
            throw new IdInvalidException("Request cannot be null");
        }
        if (request.getOrderId() == null || request.getOrderId() <= 0) {
            throw new IdInvalidException("Order id is invalid");
        }
    }

    private Order findOrderById(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with identifier: " + orderId));
    }

    private void validateOrderOwnership(Order order, User currentUser) {
        boolean isSeller = hasSellerRole(currentUser);
        if (!isSeller && !order.getUser().getId().equals(currentUser.getId())) {
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

    private boolean hasSellerRole(User user) {
        if (user.getRoles() == null || user.getRoles().isEmpty()) {
            return false;
        }
        return user.getRoles().stream()
                .anyMatch(role -> "SELLER".equals(role.getCode()));
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
        validateProcessReturnRequestRequest(request);

        ReturnRequest returnRequest = findReturnRequestById(returnRequestId);
        validateReturnRequestStatusForProcessing(returnRequest);

        User currentUser = getCurrentUser();
        validateSellerRole(currentUser);

        approveReturnRequest(returnRequest, currentUser, request.getResponseNote());
        updateOrderStatusToReturned(returnRequest.getOrder().getId());

        ReturnRequest savedReturnRequest = returnRequestRepository.save(returnRequest);
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
        validateProcessReturnRequestRequest(request);

        ReturnRequest returnRequest = findReturnRequestById(returnRequestId);
        validateReturnRequestStatusForProcessing(returnRequest);

        User currentUser = getCurrentUser();
        validateSellerRole(currentUser);

        rejectReturnRequest(returnRequest, currentUser, request.getResponseNote());

        ReturnRequest savedReturnRequest = returnRequestRepository.save(returnRequest);
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

    private void validateProcessReturnRequestRequest(ProcessReturnRequestRequest request) throws IdInvalidException {
        if (request == null) {
            throw new IdInvalidException("Request cannot be null");
        }
    }

    private void validateReturnRequestStatusForProcessing(ReturnRequest returnRequest) {
        if (returnRequest.getStatus() != ReturnRequestStatus.PENDING) {
            throw new RuntimeException("Return request can only be processed when status is PENDING. Current status: " + returnRequest.getStatus());
        }
    }

    private void validateSellerRole(User user) {
        if (user.getRoles() == null || user.getRoles().isEmpty()) {
            throw new RuntimeException("User does not have any roles. Required roles: ADMIN or SELLER");
        }

        boolean hasPermission = user.getRoles().stream()
                .anyMatch(role -> "SELLER".equals(role.getCode()) || "ADMIN".equals(role.getCode()));

        if (!hasPermission) {
            throw new RuntimeException("User does not have permission to process return requests. Required roles: ADMIN or SELLER");
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
        return userRepository.findByEmailWithRoles(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
    }

    private void validateUserIsActive(User user) {
        if (!user.isActive()) {
            throw new RuntimeException("User account is inactive. Please contact administrator.");
        }
    }
}
