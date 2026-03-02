package vn.edu.iuh.fit.bookstorebackend.service.Impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.iuh.fit.bookstorebackend.common.StockRequestStatus;
import vn.edu.iuh.fit.bookstorebackend.dto.request.ApproveStockRequestRequest;
import vn.edu.iuh.fit.bookstorebackend.dto.request.CreateStockRequestRequest;
import vn.edu.iuh.fit.bookstorebackend.dto.request.RejectStockRequestRequest;
import vn.edu.iuh.fit.bookstorebackend.dto.response.StockRequestResponse;
import vn.edu.iuh.fit.bookstorebackend.exception.IdInvalidException;
import vn.edu.iuh.fit.bookstorebackend.mapper.StockRequestMapper;
import vn.edu.iuh.fit.bookstorebackend.model.Book;
import vn.edu.iuh.fit.bookstorebackend.model.StockRequest;
import vn.edu.iuh.fit.bookstorebackend.model.User;
import vn.edu.iuh.fit.bookstorebackend.repository.BookRepository;
import vn.edu.iuh.fit.bookstorebackend.repository.StockRequestRepository;
import vn.edu.iuh.fit.bookstorebackend.repository.UserRepository;
import vn.edu.iuh.fit.bookstorebackend.service.StockRequestService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StockRequestServiceImpl implements StockRequestService {

    private final StockRequestRepository stockRequestRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final StockRequestMapper stockRequestMapper;

    @Override
    @Transactional
    public StockRequestResponse createStockRequest(CreateStockRequestRequest request) throws IdInvalidException {
        validateCreateStockRequestRequest(request);

        Book book = findBookById(request.getBookId());
        User createdBy = findUserById(request.getCreatedById());
        validateSellerRole(createdBy);

        StockRequest stockRequest = createStockRequestFromRequest(request, book, createdBy);
        StockRequest savedStockRequest = stockRequestRepository.save(stockRequest);

        return stockRequestMapper.toStockRequestResponse(savedStockRequest);
    }

    private void validateCreateStockRequestRequest(CreateStockRequestRequest request) throws IdInvalidException {
        if (request == null) {
            throw new IdInvalidException("Request cannot be null");
        }
        if (request.getBookId() == null || request.getBookId() <= 0) {
            throw new IdInvalidException("Book id is invalid");
        }
        if (request.getCreatedById() == null || request.getCreatedById() <= 0) {
            throw new IdInvalidException("User id is invalid");
        }
        if (request.getQuantity() <= 0) {
            throw new IdInvalidException("Quantity must be greater than 0");
        }
    }

    private void validateSellerRole(User createdBy) {
        if (createdBy.getRoles() == null || createdBy.getRoles().isEmpty()) {
            throw new RuntimeException("User does not have any roles. Required roles: ADMIN or SELLER");
        }

        boolean hasPermission = createdBy.getRoles().stream()
                .anyMatch(role -> "SELLER".equals(role.getCode()) || "ADMIN".equals(role.getCode()));

        if (!hasPermission) {
            throw new RuntimeException("User does not have permission to create stock requests. Required roles: ADMIN or SELLER");
        }
    }

    private StockRequest createStockRequestFromRequest(CreateStockRequestRequest request, Book book, User createdBy) {
        StockRequest stockRequest = new StockRequest();
        stockRequest.setBook(book);
        stockRequest.setQuantity(request.getQuantity());
        stockRequest.setReason(request.getReason());
        stockRequest.setStatus(StockRequestStatus.PENDING);
        stockRequest.setCreatedAt(LocalDateTime.now());
        stockRequest.setCreatedBy(createdBy);
        return stockRequest;
    }

    @Override
    @Transactional(readOnly = true)
    public List<StockRequestResponse> getMyStockRequest(Long userId) {
        validateUserId(userId);

        List<StockRequest> stockRequests = stockRequestRepository.findByCreatedBy_IdOrderByCreatedAtDesc(userId);
        return mapToStockRequestResponseList(stockRequests);
    }

    private void validateUserId(Long userId) {
        if (userId == null || userId <= 0) {
            throw new RuntimeException("User id is invalid");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<StockRequestResponse> getAllStockRequest() {
        List<StockRequest> stockRequests = stockRequestRepository.findAllByOrderByCreatedAtDesc();
        return mapToStockRequestResponseList(stockRequests);
    }

    private List<StockRequestResponse> mapToStockRequestResponseList(List<StockRequest> stockRequests) {
        return stockRequests.stream()
                .map(stockRequestMapper::toStockRequestResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public StockRequestResponse approveStockRequest(Long stockRequestId, ApproveStockRequestRequest request) throws IdInvalidException {
        validateStockRequestId(stockRequestId);
        validateApproveStockRequestRequest(request);

        StockRequest stockRequest = findStockRequestById(stockRequestId);
        validateStockRequestStatusForApproval(stockRequest);

        User processedBy = findUserById(request.getProcessedById());
        validateApproverRole(processedBy);
        
        approveStockRequest(stockRequest, processedBy, request.getResponseMessage());

        StockRequest savedStockRequest = stockRequestRepository.save(stockRequest);
        return stockRequestMapper.toStockRequestResponse(savedStockRequest);
    }

    private void validateApproveStockRequestRequest(ApproveStockRequestRequest request) throws IdInvalidException {
        if (request == null) {
            throw new IdInvalidException("Request cannot be null");
        }
        if (request.getProcessedById() == null || request.getProcessedById() <= 0) {
            throw new IdInvalidException("User id is invalid");
        }
    }

    private void validateStockRequestStatusForApproval(StockRequest stockRequest) {
        if (stockRequest.getStatus() != StockRequestStatus.PENDING) {
            throw new RuntimeException("Stock request can only be approved when status is PENDING. Current status: " + stockRequest.getStatus());
        }
    }

    private void approveStockRequest(StockRequest stockRequest, User processedBy, String responseMessage) {
        stockRequest.setStatus(StockRequestStatus.APPROVED);
        stockRequest.setProcessedAt(LocalDateTime.now());
        stockRequest.setProcessedBy(processedBy);
        stockRequest.setResponseMessage(responseMessage);
    }

    @Override
    @Transactional
    public StockRequestResponse rejectStockRequest(Long stockRequestId, RejectStockRequestRequest request) throws IdInvalidException {
        validateStockRequestId(stockRequestId);
        validateRejectStockRequestRequest(request);

        StockRequest stockRequest = findStockRequestById(stockRequestId);
        validateStockRequestStatusForRejection(stockRequest);

        User processedBy = findUserById(request.getProcessedById());
        validateApproverRole(processedBy);
        
        rejectStockRequest(stockRequest, processedBy, request.getResponseMessage());

        StockRequest savedStockRequest = stockRequestRepository.save(stockRequest);
        return stockRequestMapper.toStockRequestResponse(savedStockRequest);
    }

    private void validateRejectStockRequestRequest(RejectStockRequestRequest request) throws IdInvalidException {
        if (request == null) {
            throw new IdInvalidException("Request cannot be null");
        }
        if (request.getProcessedById() == null || request.getProcessedById() <= 0) {
            throw new IdInvalidException("User id is invalid");
        }
    }

    private void validateStockRequestStatusForRejection(StockRequest stockRequest) {
        if (stockRequest.getStatus() != StockRequestStatus.PENDING) {
            throw new RuntimeException("Stock request can only be rejected when status is PENDING. Current status: " + stockRequest.getStatus());
        }
    }

    private void rejectStockRequest(StockRequest stockRequest, User processedBy, String responseMessage) {
        stockRequest.setStatus(StockRequestStatus.REJECTED);
        stockRequest.setProcessedAt(LocalDateTime.now());
        stockRequest.setProcessedBy(processedBy);
        stockRequest.setResponseMessage(responseMessage);
    }


    private void validateStockRequestId(Long stockRequestId) throws IdInvalidException {
        if (stockRequestId == null || stockRequestId <= 0) {
            throw new IdInvalidException("Stock request id is invalid");
        }
    }

    private void validateApproverRole(User processedBy) {
        if (processedBy.getRoles() == null || processedBy.getRoles().isEmpty()) {
            throw new RuntimeException("User does not have any roles. Required roles: ADMIN or WAREHOUSE_STAFF");
        }

        boolean hasPermission = processedBy.getRoles().stream()
                .anyMatch(role -> "ADMIN".equals(role.getCode()) 
                        || "WAREHOUSE_STAFF".equals(role.getCode()));

        if (!hasPermission) {
            throw new RuntimeException("User does not have permission to approve/reject stock requests. Required roles: ADMIN or WAREHOUSE_STAFF");
        }
    }

    // Find helpers
    private Book findBookById(Long bookId) {
        return bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found with identifier: " + bookId));
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with identifier: " + userId));
    }

    private StockRequest findStockRequestById(Long stockRequestId) {
        return stockRequestRepository.findById(stockRequestId)
                .orElseThrow(() -> new RuntimeException("Stock request not found"));
    }
}
