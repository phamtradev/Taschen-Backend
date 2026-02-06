package vn.edu.iuh.fit.bookstorebackend.service.Impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.iuh.fit.bookstorebackend.dto.request.CreateSupplierRequest;
import vn.edu.iuh.fit.bookstorebackend.dto.request.UpdateSupplierRequest;
import vn.edu.iuh.fit.bookstorebackend.dto.response.SupplierResponse;
import vn.edu.iuh.fit.bookstorebackend.exception.IdInvalidException;
import vn.edu.iuh.fit.bookstorebackend.mapper.SupplierMapper;
import vn.edu.iuh.fit.bookstorebackend.model.Supplier;
import vn.edu.iuh.fit.bookstorebackend.repository.SupplierRepository;
import vn.edu.iuh.fit.bookstorebackend.service.SupplierService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SupplierServiceImpl implements SupplierService {

    private final SupplierRepository supplierRepository;
    private final SupplierMapper supplierMapper;

    @Override
    @Transactional(readOnly = true)
    public List<SupplierResponse> getAllSuppliers() {
        List<Supplier> suppliers = supplierRepository.findAll();
        return suppliers.stream()
                .map(supplierMapper::toSupplierResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public SupplierResponse createSupplier(CreateSupplierRequest request) throws IdInvalidException {
        validateCreateSupplierRequest(request);

        Supplier supplier = supplierMapper.toSupplier(request);
        Supplier savedSupplier = supplierRepository.save(supplier);

        return supplierMapper.toSupplierResponse(savedSupplier);
    }

    private void validateCreateSupplierRequest(CreateSupplierRequest request) throws IdInvalidException {
        if (request == null) {
            throw new IdInvalidException("CreateSupplierRequest cannot be null");
        }
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new IdInvalidException("Supplier name cannot be null or empty");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public SupplierResponse getSupplierById(Long supplierId) throws IdInvalidException {
        validateSupplierId(supplierId);
        Supplier supplier = findSupplierById(supplierId);
        return supplierMapper.toSupplierResponse(supplier);
    }

    @Override
    @Transactional
    public SupplierResponse updateSupplier(Long supplierId, UpdateSupplierRequest request) throws IdInvalidException {
        validateSupplierId(supplierId);
        validateUpdateSupplierRequest(request);

        Supplier supplier = findSupplierById(supplierId);
        updateSupplierFields(supplier, request);

        Supplier updatedSupplier = supplierRepository.save(supplier);
        return supplierMapper.toSupplierResponse(updatedSupplier);
    }

    private void validateUpdateSupplierRequest(UpdateSupplierRequest request) throws IdInvalidException {
        if (request == null) {
            throw new IdInvalidException("UpdateSupplierRequest cannot be null");
        }
    }

    private void updateSupplierFields(Supplier supplier, UpdateSupplierRequest request) {
        if (request.getName() != null && !request.getName().trim().isEmpty()) {
            supplier.setName(request.getName().trim());
        }
        if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
            supplier.setEmail(request.getEmail().trim());
        }
        if (request.getPhone() != null && !request.getPhone().trim().isEmpty()) {
            supplier.setPhone(request.getPhone().trim());
        }
        if (request.getAddress() != null && !request.getAddress().trim().isEmpty()) {
            supplier.setAddress(request.getAddress().trim());
        }
    }

    @Override
    @Transactional
    public void deleteSupplier(Long supplierId) throws IdInvalidException {
        validateSupplierId(supplierId);
        Supplier supplier = findSupplierById(supplierId);
        supplierRepository.delete(supplier);
    }

    private void validateSupplierId(Long supplierId) throws IdInvalidException {
        if (supplierId == null || supplierId <= 0) {
            throw new IdInvalidException("Supplier identifier is invalid: " + supplierId);
        }
    }

    private Supplier findSupplierById(Long supplierId) {
        return supplierRepository.findById(supplierId)
                .orElseThrow(() -> new RuntimeException("Supplier not found with identifier: " + supplierId));
    }
}

