package vn.edu.iuh.fit.bookstorebackend.inventory.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import vn.edu.iuh.fit.bookstorebackend.shared.dto.WsEvent;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.iuh.fit.bookstorebackend.shared.common.StockRequestStatus;
import vn.edu.iuh.fit.bookstorebackend.inventory.dto.request.ApproveStockRequestRequest;
import vn.edu.iuh.fit.bookstorebackend.inventory.dto.request.CreateStockRequestRequest;
import vn.edu.iuh.fit.bookstorebackend.inventory.dto.request.RejectStockRequestRequest;
import vn.edu.iuh.fit.bookstorebackend.inventory.dto.response.StockRequestResponse;
import vn.edu.iuh.fit.bookstorebackend.shared.exception.IdInvalidException;
import vn.edu.iuh.fit.bookstorebackend.inventory.mapper.StockRequestMapper;
import vn.edu.iuh.fit.bookstorebackend.book.model.Book;
import vn.edu.iuh.fit.bookstorebackend.inventory.model.StockRequest;
import vn.edu.iuh.fit.bookstorebackend.user.model.User;
import vn.edu.iuh.fit.bookstorebackend.book.model.Variant;
import vn.edu.iuh.fit.bookstorebackend.book.repository.BookRepository;
import vn.edu.iuh.fit.bookstorebackend.inventory.repository.StockRequestRepository;
import vn.edu.iuh.fit.bookstorebackend.user.repository.UserRepository;
import vn.edu.iuh.fit.bookstorebackend.book.repository.VariantRepository;
import vn.edu.iuh.fit.bookstorebackend.inventory.service.StockRequestService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StockRequestServiceImpl implements StockRequestService {

    private final StockRequestRepository stockRequestRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final VariantRepository variantRepository;
    private final StockRequestMapper stockRequestMapper;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    @Transactional
    public StockRequestResponse createStockRequest(CreateStockRequestRequest request) throws IdInvalidException {
        Book book = findBookById(request.getBookId());
        Variant variant = findVariantById(request.getVariantId());
        validateVariantBelongsToBook(variant, book);
        User createdBy = findUserById(request.getCreatedById());

        StockRequest stockRequest = createStockRequestFromRequest(request, book, variant, createdBy);
        StockRequest savedStockRequest = stockRequestRepository.save(stockRequest);
        messagingTemplate.convertAndSend("/topic/stock-requests",
                new WsEvent("CREATED", "STOCK_REQUEST", savedStockRequest.getId(), null));

        return stockRequestMapper.toStockRequestResponse(savedStockRequest);
    }

    private void validateVariantBelongsToBook(Variant variant, Book book) throws IdInvalidException {
        if (variant != null && book != null) {
            boolean belongsToBook = book.getBookVariants() != null &&
                    book.getBookVariants().stream()
                            .anyMatch(bv -> bv.getVariant().getId().equals(variant.getId()));
            if (!belongsToBook) {
                throw new IdInvalidException("Variant does not belong to the specified book");
            }
        }
    }

    private StockRequest createStockRequestFromRequest(CreateStockRequestRequest request, Book book, Variant variant, User createdBy) {
        StockRequest stockRequest = new StockRequest();
        stockRequest.setBook(book);
        stockRequest.setVariant(variant);
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

        StockRequest stockRequest = findStockRequestById(stockRequestId);
        validateStockRequestStatusForApproval(stockRequest);

        User processedBy = findUserById(request.getProcessedById());
        approveStockRequest(stockRequest, processedBy, request.getResponseMessage());

        StockRequest savedStockRequest = stockRequestRepository.save(stockRequest);
        return stockRequestMapper.toStockRequestResponse(savedStockRequest);
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

        StockRequest stockRequest = findStockRequestById(stockRequestId);
        validateStockRequestStatusForRejection(stockRequest);

        User processedBy = findUserById(request.getProcessedById());
        rejectStockRequest(stockRequest, processedBy, request.getResponseMessage());

        StockRequest savedStockRequest = stockRequestRepository.save(stockRequest);
        messagingTemplate.convertAndSend("/topic/stock-requests",
                new WsEvent("UPDATED", "STOCK_REQUEST", savedStockRequest.getId(), null));
        return stockRequestMapper.toStockRequestResponse(savedStockRequest);
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

    private Variant findVariantById(Long variantId) {
        if (variantId == null) {
            return null;
        }
        return variantRepository.findById(variantId)
                .orElseThrow(() -> new RuntimeException("Variant not found with identifier: " + variantId));
    }
}
