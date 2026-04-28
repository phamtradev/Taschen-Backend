package vn.edu.iuh.fit.bookstorebackend.inventory.service.impl;
import vn.edu.iuh.fit.bookstorebackend.inventory.model.Batch;
import vn.edu.iuh.fit.bookstorebackend.inventory.model.DisposalRequest;
import vn.edu.iuh.fit.bookstorebackend.inventory.model.DisposalRequestItem;
import vn.edu.iuh.fit.bookstorebackend.inventory.repository.BatchRepository;
import vn.edu.iuh.fit.bookstorebackend.inventory.repository.DisposalRequestRepository;
import vn.edu.iuh.fit.bookstorebackend.user.model.User;
import vn.edu.iuh.fit.bookstorebackend.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.iuh.fit.bookstorebackend.shared.common.DisposalRequestStatus;
import vn.edu.iuh.fit.bookstorebackend.inventory.dto.request.CreateDisposalRequestRequest;
import vn.edu.iuh.fit.bookstorebackend.inventory.dto.request.ProcessDisposalRequestRequest;
import vn.edu.iuh.fit.bookstorebackend.inventory.dto.response.DisposalRequestResponse;
import vn.edu.iuh.fit.bookstorebackend.shared.exception.IdInvalidException;
import vn.edu.iuh.fit.bookstorebackend.inventory.mapper.DisposalRequestMapper;
import vn.edu.iuh.fit.bookstorebackend.inventory.service.BatchService;
import vn.edu.iuh.fit.bookstorebackend.inventory.service.DisposalRequestService;

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
    private final BatchRepository batchRepository;
    private final UserRepository userRepository;
    private final BatchService batchService;
    private final DisposalRequestMapper disposalRequestMapper;

    @Override
    @Transactional
    public DisposalRequestResponse createDisposalRequest(CreateDisposalRequestRequest request) throws IdInvalidException {
        User currentUser = getCurrentUser();

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

        disposalRequest.setStatus(DisposalRequestStatus.APPROVED);
        disposalRequest.setProcessedBy(currentUser);
        disposalRequest.setProcessedAt(LocalDateTime.now());
        disposalRequest.setResponseNote(request.getResponseNote());

        Set<Long> bookIdsToSync = new HashSet<>();
        for (DisposalRequestItem item : disposalRequest.getItems()) {
            Batch batch = item.getBatch();
            batch.setRemainingQuantity(batch.getRemainingQuantity() - item.getQuantity());
            batchRepository.save(batch);

            item.setRemainingQuantityAfter(batch.getRemainingQuantity());
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
        return userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User information not found"));
    }
}
