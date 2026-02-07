package vn.edu.iuh.fit.bookstorebackend.service;

import vn.edu.iuh.fit.bookstorebackend.dto.request.ApproveStockRequestRequest;
import vn.edu.iuh.fit.bookstorebackend.dto.request.CreateStockRequestRequest;
import vn.edu.iuh.fit.bookstorebackend.dto.request.RejectStockRequestRequest;
import vn.edu.iuh.fit.bookstorebackend.dto.response.StockRequestResponse;
import vn.edu.iuh.fit.bookstorebackend.exception.IdInvalidException;

import java.util.List;

public interface StockRequestService {

    StockRequestResponse createStockRequest(CreateStockRequestRequest request) throws IdInvalidException;

    List<StockRequestResponse> getMyStockRequest(Long userId);

    List<StockRequestResponse> getAllStockRequest();

    StockRequestResponse approveStockRequest(Long stockRequestId, ApproveStockRequestRequest request) throws IdInvalidException;

    StockRequestResponse rejectStockRequest(Long stockRequestId, RejectStockRequestRequest request) throws IdInvalidException;
}
