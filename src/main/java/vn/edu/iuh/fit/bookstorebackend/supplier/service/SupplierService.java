package vn.edu.iuh.fit.bookstorebackend.supplier.service;

import vn.edu.iuh.fit.bookstorebackend.supplier.dto.request.CreateSupplierRequest;
import vn.edu.iuh.fit.bookstorebackend.supplier.dto.request.UpdateSupplierRequest;
import vn.edu.iuh.fit.bookstorebackend.supplier.dto.response.SupplierResponse;
import vn.edu.iuh.fit.bookstorebackend.shared.exception.IdInvalidException;

import java.util.List;

public interface SupplierService {

    List<SupplierResponse> getAllSuppliers();

    SupplierResponse createSupplier(CreateSupplierRequest request) throws IdInvalidException;

    SupplierResponse getSupplierById(Long supplierId) throws IdInvalidException;

    SupplierResponse updateSupplier(Long supplierId, UpdateSupplierRequest request) throws IdInvalidException;

    void deleteSupplier(Long supplierId) throws IdInvalidException;
}

