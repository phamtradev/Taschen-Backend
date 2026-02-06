package vn.edu.iuh.fit.bookstorebackend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.iuh.fit.bookstorebackend.dto.request.CreateImportStockRequest;
import vn.edu.iuh.fit.bookstorebackend.dto.response.ImportStockResponse;
import vn.edu.iuh.fit.bookstorebackend.exception.IdInvalidException;
import vn.edu.iuh.fit.bookstorebackend.service.ImportStockService;

import java.util.List;

@RestController
@RequestMapping("/api/import-stocks")
public class ImportStockController {

    private final ImportStockService importStockService;

    public ImportStockController(ImportStockService importStockService) {
        this.importStockService = importStockService;
    }

    @PostMapping
    public ResponseEntity<ImportStockResponse> createImportStock(
            @RequestBody CreateImportStockRequest request) throws IdInvalidException {
        ImportStockResponse importStockResponse = importStockService.createImportStock(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(importStockResponse);
    }

    @GetMapping
    public ResponseEntity<List<ImportStockResponse>> getAllImportStocks() {
        List<ImportStockResponse> importStocks = importStockService.getAllImportStocks();
        return ResponseEntity.status(HttpStatus.OK).body(importStocks);
    }

    @GetMapping("/book/{bookId}")
    public ResponseEntity<List<ImportStockResponse>> getImportHistoryByBookId(
            @PathVariable Long bookId) throws IdInvalidException {
        List<ImportStockResponse> importStocks = importStockService.getImportHistoryByBookId(bookId);
        return ResponseEntity.status(HttpStatus.OK).body(importStocks);
    }
}
