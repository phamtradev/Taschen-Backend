package vn.edu.iuh.fit.bookstorebackend.inventory.service;

import vn.edu.iuh.fit.bookstorebackend.inventory.dto.request.CreateDisposalRequestRequest;
import vn.edu.iuh.fit.bookstorebackend.inventory.dto.request.ProcessDisposalRequestRequest;
import vn.edu.iuh.fit.bookstorebackend.inventory.dto.response.DisposalRequestResponse;
import vn.edu.iuh.fit.bookstorebackend.shared.exception.IdInvalidException;

import java.util.List;

public interface DisposalRequestService {

    DisposalRequestResponse createDisposalRequest(CreateDisposalRequestRequest request) throws IdInvalidException;

    List<DisposalRequestResponse> getMyDisposalRequests();

    List<DisposalRequestResponse> getAllDisposalRequests();

    DisposalRequestResponse getDisposalRequestById(Long id) throws IdInvalidException;

    DisposalRequestResponse approveDisposalRequest(Long id, ProcessDisposalRequestRequest request) throws IdInvalidException;

    DisposalRequestResponse rejectDisposalRequest(Long id, ProcessDisposalRequestRequest request) throws IdInvalidException;
}
