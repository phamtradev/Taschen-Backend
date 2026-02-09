package vn.edu.iuh.fit.bookstorebackend.service;

import vn.edu.iuh.fit.bookstorebackend.dto.request.CreateDisposalRequestRequest;
import vn.edu.iuh.fit.bookstorebackend.dto.request.ProcessDisposalRequestRequest;
import vn.edu.iuh.fit.bookstorebackend.dto.response.DisposalRequestResponse;
import vn.edu.iuh.fit.bookstorebackend.exception.IdInvalidException;

import java.util.List;

public interface DisposalRequestService {

    DisposalRequestResponse createDisposalRequest(CreateDisposalRequestRequest request) throws IdInvalidException;

    List<DisposalRequestResponse> getMyDisposalRequests();

    List<DisposalRequestResponse> getAllDisposalRequests();

    DisposalRequestResponse getDisposalRequestById(Long id) throws IdInvalidException;

    DisposalRequestResponse approveDisposalRequest(Long id, ProcessDisposalRequestRequest request) throws IdInvalidException;

    DisposalRequestResponse rejectDisposalRequest(Long id, ProcessDisposalRequestRequest request) throws IdInvalidException;
}
