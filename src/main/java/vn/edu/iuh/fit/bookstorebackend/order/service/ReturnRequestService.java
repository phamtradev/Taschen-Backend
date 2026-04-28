package vn.edu.iuh.fit.bookstorebackend.order.service;

import vn.edu.iuh.fit.bookstorebackend.order.dto.request.CreateReturnRequestRequest;
import vn.edu.iuh.fit.bookstorebackend.order.dto.request.ProcessReturnRequestRequest;
import vn.edu.iuh.fit.bookstorebackend.order.dto.response.ReturnRequestResponse;
import vn.edu.iuh.fit.bookstorebackend.shared.exception.IdInvalidException;

import java.util.List;

public interface ReturnRequestService {

    ReturnRequestResponse createReturnRequest(CreateReturnRequestRequest request) throws IdInvalidException;

    List<ReturnRequestResponse> getMyReturnRequests();

    List<ReturnRequestResponse> getAllReturnRequests();

    ReturnRequestResponse approveReturnRequest(Long returnRequestId, ProcessReturnRequestRequest request) throws IdInvalidException;

    ReturnRequestResponse rejectReturnRequest(Long returnRequestId, ProcessReturnRequestRequest request) throws IdInvalidException;
}
