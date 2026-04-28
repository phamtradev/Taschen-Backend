package vn.edu.iuh.fit.bookstorebackend.inventory.service;

import vn.edu.iuh.fit.bookstorebackend.inventory.dto.request.CreateImportStockRequest;
import vn.edu.iuh.fit.bookstorebackend.inventory.dto.response.ImportStockResponse;
import vn.edu.iuh.fit.bookstorebackend.inventory.dto.response.ReceiveStockResponse;
import vn.edu.iuh.fit.bookstorebackend.shared.exception.IdInvalidException;

import java.util.List;

public interface ImportStockService {

    ImportStockResponse createImportStock(CreateImportStockRequest request) throws IdInvalidException;

    ImportStockResponse getImportStockById(Long id) throws IdInvalidException;

    List<ImportStockResponse> getAllImportStocks();

    List<ImportStockResponse> getImportHistoryByBookId(Long bookId) throws IdInvalidException;

    ReceiveStockResponse receiveStock(Long importStockId, Long userId) throws IdInvalidException;
}
