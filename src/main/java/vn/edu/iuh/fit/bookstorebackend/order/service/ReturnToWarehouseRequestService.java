package vn.edu.iuh.fit.bookstorebackend.order.service;

import vn.edu.iuh.fit.bookstorebackend.order.dto.request.CreateReturnToWarehouseRequestRequest;
import vn.edu.iuh.fit.bookstorebackend.order.dto.request.ProcessReturnToWarehouseRequestRequest;
import vn.edu.iuh.fit.bookstorebackend.order.dto.response.ReturnToWarehouseRequestResponse;
import vn.edu.iuh.fit.bookstorebackend.shared.exception.IdInvalidException;

import java.util.List;

public interface ReturnToWarehouseRequestService {

    ReturnToWarehouseRequestResponse createReturnToWarehouseRequest(CreateReturnToWarehouseRequestRequest request) throws IdInvalidException;

    List<ReturnToWarehouseRequestResponse> getMyReturnToWarehouseRequests();

    List<ReturnToWarehouseRequestResponse> getAllReturnToWarehouseRequests();

    ReturnToWarehouseRequestResponse approveReturnToWarehouseRequest(Long returnToWarehouseRequestId, ProcessReturnToWarehouseRequestRequest request) throws IdInvalidException;

    ReturnToWarehouseRequestResponse rejectReturnToWarehouseRequest(Long returnToWarehouseRequestId, ProcessReturnToWarehouseRequestRequest request) throws IdInvalidException;
}
