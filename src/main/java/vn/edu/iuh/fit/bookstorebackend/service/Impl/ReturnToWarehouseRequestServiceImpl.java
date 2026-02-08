package vn.edu.iuh.fit.bookstorebackend.service.Impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.iuh.fit.bookstorebackend.common.ReturnToWarehouseRequestStatus;
import vn.edu.iuh.fit.bookstorebackend.dto.request.CreateReturnToWarehouseRequestRequest;
import vn.edu.iuh.fit.bookstorebackend.dto.request.ProcessReturnToWarehouseRequestRequest;
import vn.edu.iuh.fit.bookstorebackend.dto.response.ReturnToWarehouseRequestResponse;
import vn.edu.iuh.fit.bookstorebackend.exception.IdInvalidException;
import vn.edu.iuh.fit.bookstorebackend.mapper.ReturnToWarehouseRequestMapper;
import vn.edu.iuh.fit.bookstorebackend.model.Book;
import vn.edu.iuh.fit.bookstorebackend.model.ReturnToWarehouseRequest;
import vn.edu.iuh.fit.bookstorebackend.model.User;
import vn.edu.iuh.fit.bookstorebackend.repository.BookRepository;
import vn.edu.iuh.fit.bookstorebackend.repository.ReturnToWarehouseRequestRepository;
import vn.edu.iuh.fit.bookstorebackend.repository.UserRepository;
import vn.edu.iuh.fit.bookstorebackend.service.ReturnToWarehouseRequestService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReturnToWarehouseRequestServiceImpl implements ReturnToWarehouseRequestService {

    private final ReturnToWarehouseRequestRepository returnToWarehouseRequestRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final ReturnToWarehouseRequestMapper returnToWarehouseRequestMapper;

    @Override
    @Transactional
    public ReturnToWarehouseRequestResponse createReturnToWarehouseRequest(CreateReturnToWarehouseRequestRequest request) throws IdInvalidException {
        // Role: SELLER - Tạo yêu cầu trả hàng về kho
        validateCreateReturnToWarehouseRequestRequest(request);

        User currentUser = getCurrentUser();
        validateSellerRole(currentUser);

        Book book = findBookById(request.getBookId());

        ReturnToWarehouseRequest returnToWarehouseRequest = createReturnToWarehouseRequestFromRequest(request, book, currentUser);
        ReturnToWarehouseRequest savedReturnToWarehouseRequest = returnToWarehouseRequestRepository.save(returnToWarehouseRequest);

        return returnToWarehouseRequestMapper.toReturnToWarehouseRequestResponse(savedReturnToWarehouseRequest);
    }

    private void validateCreateReturnToWarehouseRequestRequest(CreateReturnToWarehouseRequestRequest request) throws IdInvalidException {
        if (request == null) {
            throw new IdInvalidException("Request cannot be null");
        }
        if (request.getBookId() == null || request.getBookId() <= 0) {
            throw new IdInvalidException("Book id is invalid");
        }
        if (request.getQuantity() <= 0) {
            throw new IdInvalidException("Quantity must be greater than 0");
        }
    }

    private void validateSellerRole(User user) {
        if (user.getRoles() == null || user.getRoles().isEmpty()) {
            throw new RuntimeException("User does not have any roles. Required role: SELLER");
        }

        boolean hasPermission = user.getRoles().stream()
                .anyMatch(role -> "SELLER".equals(role.getCode()));

        if (!hasPermission) {
            throw new RuntimeException("User does not have permission to create return to warehouse requests. Required role: SELLER");
        }
    }

    private ReturnToWarehouseRequest createReturnToWarehouseRequestFromRequest(CreateReturnToWarehouseRequestRequest request, Book book, User createdBy) {
        ReturnToWarehouseRequest returnToWarehouseRequest = new ReturnToWarehouseRequest();
        returnToWarehouseRequest.setBook(book);
        returnToWarehouseRequest.setQuantity(request.getQuantity());
        returnToWarehouseRequest.setReason(request.getReason());
        returnToWarehouseRequest.setStatus(ReturnToWarehouseRequestStatus.PENDING);
        returnToWarehouseRequest.setCreatedBy(createdBy);
        returnToWarehouseRequest.setCreatedAt(LocalDateTime.now());
        return returnToWarehouseRequest;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReturnToWarehouseRequestResponse> getMyReturnToWarehouseRequests() {
        User currentUser = getCurrentUser();
        List<ReturnToWarehouseRequest> returnToWarehouseRequests = returnToWarehouseRequestRepository.findByCreatedBy_IdOrderByCreatedAtDesc(currentUser.getId());
        return mapToReturnToWarehouseRequestResponseList(returnToWarehouseRequests);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReturnToWarehouseRequestResponse> getAllReturnToWarehouseRequests() {
        List<ReturnToWarehouseRequest> returnToWarehouseRequests = returnToWarehouseRequestRepository.findAllByOrderByCreatedAtDesc();
        return mapToReturnToWarehouseRequestResponseList(returnToWarehouseRequests);
    }

    private List<ReturnToWarehouseRequestResponse> mapToReturnToWarehouseRequestResponseList(List<ReturnToWarehouseRequest> returnToWarehouseRequests) {
        return returnToWarehouseRequests.stream()
                .map(returnToWarehouseRequestMapper::toReturnToWarehouseRequestResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ReturnToWarehouseRequestResponse approveReturnToWarehouseRequest(Long returnToWarehouseRequestId, ProcessReturnToWarehouseRequestRequest request) throws IdInvalidException {
        // Role: ADMIN hoặc WAREHOUSE_STAFF - Duyệt yêu cầu trả hàng về kho và cộng tồn kho
        validateReturnToWarehouseRequestId(returnToWarehouseRequestId);
        validateProcessReturnToWarehouseRequestRequest(request);

        ReturnToWarehouseRequest returnToWarehouseRequest = findReturnToWarehouseRequestById(returnToWarehouseRequestId);
        validateReturnToWarehouseRequestStatusForProcessing(returnToWarehouseRequest);

        User currentUser = getCurrentUser();
        validateApproverRole(currentUser);

        approveReturnToWarehouseRequest(returnToWarehouseRequest, currentUser, request.getResponseNote());
        updateBookStockQuantity(returnToWarehouseRequest.getBook(), returnToWarehouseRequest.getQuantity());

        ReturnToWarehouseRequest savedReturnToWarehouseRequest = returnToWarehouseRequestRepository.save(returnToWarehouseRequest);
        return returnToWarehouseRequestMapper.toReturnToWarehouseRequestResponse(savedReturnToWarehouseRequest);
    }

    private void approveReturnToWarehouseRequest(ReturnToWarehouseRequest returnToWarehouseRequest, User processedBy, String responseNote) {
        returnToWarehouseRequest.setStatus(ReturnToWarehouseRequestStatus.APPROVED);
        returnToWarehouseRequest.setProcessedBy(processedBy);
        returnToWarehouseRequest.setProcessedAt(LocalDateTime.now());
        returnToWarehouseRequest.setResponseNote(responseNote);
    }

    private void updateBookStockQuantity(Book book, int quantity) {
        book.setStockQuantity(book.getStockQuantity() + quantity);
        bookRepository.save(book);
    }

    @Override
    @Transactional
    public ReturnToWarehouseRequestResponse rejectReturnToWarehouseRequest(Long returnToWarehouseRequestId, ProcessReturnToWarehouseRequestRequest request) throws IdInvalidException {
        // Role: ADMIN hoặc WAREHOUSE_STAFF - Từ chối yêu cầu trả hàng về kho
        validateReturnToWarehouseRequestId(returnToWarehouseRequestId);
        validateProcessReturnToWarehouseRequestRequest(request);

        ReturnToWarehouseRequest returnToWarehouseRequest = findReturnToWarehouseRequestById(returnToWarehouseRequestId);
        validateReturnToWarehouseRequestStatusForProcessing(returnToWarehouseRequest);

        User currentUser = getCurrentUser();
        validateApproverRole(currentUser);

        rejectReturnToWarehouseRequest(returnToWarehouseRequest, currentUser, request.getResponseNote());

        ReturnToWarehouseRequest savedReturnToWarehouseRequest = returnToWarehouseRequestRepository.save(returnToWarehouseRequest);
        return returnToWarehouseRequestMapper.toReturnToWarehouseRequestResponse(savedReturnToWarehouseRequest);
    }

    private void rejectReturnToWarehouseRequest(ReturnToWarehouseRequest returnToWarehouseRequest, User processedBy, String responseNote) {
        returnToWarehouseRequest.setStatus(ReturnToWarehouseRequestStatus.REJECTED);
        returnToWarehouseRequest.setProcessedBy(processedBy);
        returnToWarehouseRequest.setProcessedAt(LocalDateTime.now());
        returnToWarehouseRequest.setResponseNote(responseNote);
    }

    private void validateReturnToWarehouseRequestId(Long returnToWarehouseRequestId) throws IdInvalidException {
        if (returnToWarehouseRequestId == null || returnToWarehouseRequestId <= 0) {
            throw new IdInvalidException("Return to warehouse request id is invalid");
        }
    }

    private void validateProcessReturnToWarehouseRequestRequest(ProcessReturnToWarehouseRequestRequest request) throws IdInvalidException {
        if (request == null) {
            throw new IdInvalidException("Request cannot be null");
        }
    }

    private void validateReturnToWarehouseRequestStatusForProcessing(ReturnToWarehouseRequest returnToWarehouseRequest) {
        if (returnToWarehouseRequest.getStatus() != ReturnToWarehouseRequestStatus.PENDING) {
            throw new RuntimeException("Return to warehouse request can only be processed when status is PENDING. Current status: " + returnToWarehouseRequest.getStatus());
        }
    }

    private void validateApproverRole(User user) {
        if (user.getRoles() == null || user.getRoles().isEmpty()) {
            throw new RuntimeException("User does not have any roles. Required roles: ADMIN or WAREHOUSE_STAFF");
        }

        boolean hasPermission = user.getRoles().stream()
                .anyMatch(role -> "ADMIN".equals(role.getCode()) 
                        || "WAREHOUSE_STAFF".equals(role.getCode()));

        if (!hasPermission) {
            throw new RuntimeException("User does not have permission to process return to warehouse requests. Required roles: ADMIN or WAREHOUSE_STAFF");
        }
    }

    private ReturnToWarehouseRequest findReturnToWarehouseRequestById(Long returnToWarehouseRequestId) {
        return returnToWarehouseRequestRepository.findById(returnToWarehouseRequestId)
                .orElseThrow(() -> new RuntimeException("Return to warehouse request not found"));
    }

    private Book findBookById(Long bookId) {
        return bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found with identifier: " + bookId));
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
