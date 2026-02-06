package vn.edu.iuh.fit.bookstorebackend.service;

import vn.edu.iuh.fit.bookstorebackend.dto.request.CreateSupplierRequest;
import vn.edu.iuh.fit.bookstorebackend.dto.request.UpdateSupplierRequest;
import vn.edu.iuh.fit.bookstorebackend.dto.response.SupplierResponse;
import vn.edu.iuh.fit.bookstorebackend.exception.IdInvalidException;

import java.util.List;

public interface SupplierService {

    List<SupplierResponse> getAllSuppliers();

    SupplierResponse createSupplier(CreateSupplierRequest request) throws IdInvalidException;

    SupplierResponse getSupplierById(Long supplierId) throws IdInvalidException;

    SupplierResponse updateSupplier(Long supplierId, UpdateSupplierRequest request) throws IdInvalidException;

    void deleteSupplier(Long supplierId) throws IdInvalidException;
}

