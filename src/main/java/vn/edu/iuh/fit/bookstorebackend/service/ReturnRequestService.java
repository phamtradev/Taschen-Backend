package vn.edu.iuh.fit.bookstorebackend.service;

import vn.edu.iuh.fit.bookstorebackend.dto.request.CreateReturnRequestRequest;
import vn.edu.iuh.fit.bookstorebackend.dto.request.ProcessReturnRequestRequest;
import vn.edu.iuh.fit.bookstorebackend.dto.response.ReturnRequestResponse;
import vn.edu.iuh.fit.bookstorebackend.exception.IdInvalidException;

import java.util.List;

public interface ReturnRequestService {

    ReturnRequestResponse createReturnRequest(CreateReturnRequestRequest request) throws IdInvalidException;

    List<ReturnRequestResponse> getMyReturnRequests();

    List<ReturnRequestResponse> getAllReturnRequests();

    ReturnRequestResponse approveReturnRequest(Long returnRequestId, ProcessReturnRequestRequest request) throws IdInvalidException;

    ReturnRequestResponse rejectReturnRequest(Long returnRequestId, ProcessReturnRequestRequest request) throws IdInvalidException;
}
