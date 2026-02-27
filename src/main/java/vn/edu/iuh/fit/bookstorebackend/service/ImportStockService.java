package vn.edu.iuh.fit.bookstorebackend.service;

import vn.edu.iuh.fit.bookstorebackend.dto.request.CreateImportStockRequest;
import vn.edu.iuh.fit.bookstorebackend.dto.response.ImportStockResponse;
import vn.edu.iuh.fit.bookstorebackend.dto.response.ReceiveStockResponse;
import vn.edu.iuh.fit.bookstorebackend.exception.IdInvalidException;

import java.util.List;

public interface ImportStockService {

    ImportStockResponse createImportStock(CreateImportStockRequest request) throws IdInvalidException;

    List<ImportStockResponse> getAllImportStocks();

    List<ImportStockResponse> getImportHistoryByBookId(Long bookId) throws IdInvalidException;

    ReceiveStockResponse receiveStock(Long importStockId, Long userId) throws IdInvalidException;
}
