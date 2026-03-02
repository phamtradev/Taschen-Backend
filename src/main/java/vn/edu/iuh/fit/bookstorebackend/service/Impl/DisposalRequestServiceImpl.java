package vn.edu.iuh.fit.bookstorebackend.service.Impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.iuh.fit.bookstorebackend.common.DisposalRequestStatus;
import vn.edu.iuh.fit.bookstorebackend.dto.request.CreateDisposalRequestRequest;
import vn.edu.iuh.fit.bookstorebackend.dto.request.ProcessDisposalRequestRequest;
import vn.edu.iuh.fit.bookstorebackend.dto.response.DisposalRequestResponse;
import vn.edu.iuh.fit.bookstorebackend.exception.IdInvalidException;
import vn.edu.iuh.fit.bookstorebackend.mapper.DisposalRequestMapper;
import vn.edu.iuh.fit.bookstorebackend.model.*;
import vn.edu.iuh.fit.bookstorebackend.repository.*;
import vn.edu.iuh.fit.bookstorebackend.service.BatchService;
import vn.edu.iuh.fit.bookstorebackend.service.DisposalRequestService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DisposalRequestServiceImpl implements DisposalRequestService {

    private final DisposalRequestRepository disposalRequestRepository;
    private final DisposalRequestItemRepository disposalRequestItemRepository;
    private final BatchRepository batchRepository;
    private final UserRepository userRepository;
    private final BatchService batchService;
    private final DisposalRequestMapper disposalRequestMapper;

    @Override
    @Transactional
    public DisposalRequestResponse createDisposalRequest(CreateDisposalRequestRequest request) throws IdInvalidException {
        validateCreateRequest(request);
        User currentUser = getCurrentUser();
        validateWarehouseStaffRole(currentUser);

        DisposalRequest disposalRequest = new DisposalRequest();
        disposalRequest.setReason(request.getReason());
        disposalRequest.setStatus(DisposalRequestStatus.PENDING);
        disposalRequest.setCreatedAt(LocalDateTime.now());
        disposalRequest.setCreatedBy(currentUser);

        List<DisposalRequestItem> items = new ArrayList<>();
        for (CreateDisposalRequestRequest.DisposalItemRequest itemReq : request.getItems()) {
            Batch batch = findBatchById(itemReq.getBatchId());
            validateBatchStock(batch, itemReq.getQuantity());

            DisposalRequestItem item = new DisposalRequestItem();
            item.setDisposalRequest(disposalRequest);
            item.setBatch(batch);
            item.setQuantity(itemReq.getQuantity());
            items.add(item);
        }
        disposalRequest.setItems(items);

        DisposalRequest saved = disposalRequestRepository.save(disposalRequest);
        return disposalRequestMapper.toResponse(saved);
    }

    private void validateCreateRequest(CreateDisposalRequestRequest request) throws IdInvalidException {
        if (request == null || request.getItems() == null || request.getItems().isEmpty()) {
            throw new IdInvalidException("Invalid disposal request or missing item details");
        }
    }

    private void validateBatchStock(Batch batch, int quantity) {
        if (batch.getRemainingQuantity() < quantity) {
            throw new IllegalStateException("Batch " + batch.getBatchCode() + " does not have enough quantity to dispose. Current available: " + batch.getRemainingQuantity());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<DisposalRequestResponse> getMyDisposalRequests() {
        User currentUser = getCurrentUser();
        return disposalRequestRepository.findByCreatedBy_IdOrderByCreatedAtDesc(currentUser.getId())
                .stream().map(disposalRequestMapper::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<DisposalRequestResponse> getAllDisposalRequests() {
        return disposalRequestRepository.findAllByOrderByCreatedAtDesc()
                .stream().map(disposalRequestMapper::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public DisposalRequestResponse getDisposalRequestById(Long id) throws IdInvalidException {
        DisposalRequest request = findDisposalRequestById(id);
        return disposalRequestMapper.toResponse(request);
    }

    @Override
    @Transactional
    public DisposalRequestResponse approveDisposalRequest(Long id, ProcessDisposalRequestRequest request) throws IdInvalidException {
        DisposalRequest disposalRequest = findDisposalRequestById(id);
        validateStatusPending(disposalRequest);
        User currentUser = getCurrentUser();
        validateAdminRole(currentUser);

        disposalRequest.setStatus(DisposalRequestStatus.APPROVED);
        disposalRequest.setProcessedBy(currentUser);
        disposalRequest.setProcessedAt(LocalDateTime.now());
        disposalRequest.setResponseNote(request.getResponseNote());

        Set<Long> bookIdsToSync = new HashSet<>();
        for (DisposalRequestItem item : disposalRequest.getItems()) {
            Batch batch = item.getBatch();
            batch.setRemainingQuantity(batch.getRemainingQuantity() - item.getQuantity());
            batchRepository.save(batch);
            bookIdsToSync.add(batch.getBook().getId());
        }

        for (Long bookId : bookIdsToSync) {
            batchService.syncBookStockQuantity(bookId);
        }

        return disposalRequestMapper.toResponse(disposalRequestRepository.save(disposalRequest));
    }

    @Override
    @Transactional
    public DisposalRequestResponse rejectDisposalRequest(Long id, ProcessDisposalRequestRequest request) throws IdInvalidException {
        DisposalRequest disposalRequest = findDisposalRequestById(id);
        validateStatusPending(disposalRequest);
        User currentUser = getCurrentUser();
        validateAdminRole(currentUser);

        disposalRequest.setStatus(DisposalRequestStatus.REJECTED);
        disposalRequest.setProcessedBy(currentUser);
        disposalRequest.setProcessedAt(LocalDateTime.now());
        disposalRequest.setResponseNote(request.getResponseNote());

        return disposalRequestMapper.toResponse(disposalRequestRepository.save(disposalRequest));
    }

    private void validateStatusPending(DisposalRequest request) {
        if (request.getStatus() != DisposalRequestStatus.PENDING) {
            throw new IllegalStateException("This request has already been processed");
        }
    }

    private DisposalRequest findDisposalRequestById(Long id) throws IdInvalidException {
        return disposalRequestRepository.findById(id)
                .orElseThrow(() -> new IdInvalidException("Disposal request not found with id: " + id));
    }

    private Batch findBatchById(Long id) throws IdInvalidException {
        return batchRepository.findById(id)
                .orElseThrow(() -> new IdInvalidException("Batch not found with id: " + id));
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            throw new RuntimeException("User is not authenticated");
        }
        return userRepository.findByEmailWithRoles(auth.getName())
                .orElseThrow(() -> new RuntimeException("User information not found"));
    }

    private void validateWarehouseStaffRole(User user) {
        boolean isStaff = user.getRoles().stream()
                .anyMatch(r -> r.getCode().equals("WAREHOUSE_STAFF") || r.getCode().equals("ADMIN"));
        if (!isStaff) {
            throw new RuntimeException("Only warehouse staff or Admin have permission to create this request");
        }
    }

    private void validateAdminRole(User user) {
        boolean isAdmin = user.getRoles().stream()
                .anyMatch(r -> r.getCode().equals("ADMIN"));
        if (!isAdmin) {
            throw new RuntimeException("Only Admin has permission to process this request");
        }
    }
}
