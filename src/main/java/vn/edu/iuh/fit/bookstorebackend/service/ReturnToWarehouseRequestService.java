package vn.edu.iuh.fit.bookstorebackend.service;

import vn.edu.iuh.fit.bookstorebackend.dto.request.CreateReturnToWarehouseRequestRequest;
import vn.edu.iuh.fit.bookstorebackend.dto.request.ProcessReturnToWarehouseRequestRequest;
import vn.edu.iuh.fit.bookstorebackend.dto.response.ReturnToWarehouseRequestResponse;
import vn.edu.iuh.fit.bookstorebackend.exception.IdInvalidException;

import java.util.List;

public interface ReturnToWarehouseRequestService {

    ReturnToWarehouseRequestResponse createReturnToWarehouseRequest(CreateReturnToWarehouseRequestRequest request) throws IdInvalidException;

    List<ReturnToWarehouseRequestResponse> getMyReturnToWarehouseRequests();

    List<ReturnToWarehouseRequestResponse> getAllReturnToWarehouseRequests();

    ReturnToWarehouseRequestResponse approveReturnToWarehouseRequest(Long returnToWarehouseRequestId, ProcessReturnToWarehouseRequestRequest request) throws IdInvalidException;

    ReturnToWarehouseRequestResponse rejectReturnToWarehouseRequest(Long returnToWarehouseRequestId, ProcessReturnToWarehouseRequestRequest request) throws IdInvalidException;
}
