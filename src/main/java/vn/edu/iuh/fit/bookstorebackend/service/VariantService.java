package vn.edu.iuh.fit.bookstorebackend.service;

import vn.edu.iuh.fit.bookstorebackend.dto.request.CreateVariantRequest;
import vn.edu.iuh.fit.bookstorebackend.dto.request.UpdateVariantRequest;
import vn.edu.iuh.fit.bookstorebackend.dto.response.VariantResponse;
import vn.edu.iuh.fit.bookstorebackend.exception.IdInvalidException;

import java.util.List;

public interface VariantService {
    
    VariantResponse createVariant(CreateVariantRequest request) throws IdInvalidException;
    
    VariantResponse getVariantById(Long variantId) throws IdInvalidException;
    
    List<VariantResponse> getAllVariants();
    
    List<VariantResponse> getVariantsByBookId(Long bookId) throws IdInvalidException;
    
    VariantResponse updateVariant(Long variantId, UpdateVariantRequest request) throws IdInvalidException;
    
    void deleteVariant(Long variantId) throws IdInvalidException;
}
