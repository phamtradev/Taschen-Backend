package vn.edu.iuh.fit.bookstorebackend.inventory.service;

import vn.edu.iuh.fit.bookstorebackend.inventory.dto.request.ApproveStockRequestRequest;
import vn.edu.iuh.fit.bookstorebackend.inventory.dto.request.CreateStockRequestRequest;
import vn.edu.iuh.fit.bookstorebackend.inventory.dto.request.RejectStockRequestRequest;
import vn.edu.iuh.fit.bookstorebackend.inventory.dto.response.StockRequestResponse;
import vn.edu.iuh.fit.bookstorebackend.shared.exception.IdInvalidException;

import java.util.List;

public interface StockRequestService {

    StockRequestResponse createStockRequest(CreateStockRequestRequest request) throws IdInvalidException;

    List<StockRequestResponse> getMyStockRequest(Long userId);

    List<StockRequestResponse> getAllStockRequest();

    StockRequestResponse approveStockRequest(Long stockRequestId, ApproveStockRequestRequest request) throws IdInvalidException;

    StockRequestResponse rejectStockRequest(Long stockRequestId, RejectStockRequestRequest request) throws IdInvalidException;
}
