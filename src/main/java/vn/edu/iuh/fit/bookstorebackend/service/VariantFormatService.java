package vn.edu.iuh.fit.bookstorebackend.service;

import vn.edu.iuh.fit.bookstorebackend.dto.request.CreateVariantFormatRequest;
import vn.edu.iuh.fit.bookstorebackend.dto.request.UpdateVariantFormatRequest;
import vn.edu.iuh.fit.bookstorebackend.dto.response.VariantFormatResponse;
import vn.edu.iuh.fit.bookstorebackend.exception.IdInvalidException;

import java.util.List;

public interface VariantFormatService {
    
    VariantFormatResponse createVariantFormat(CreateVariantFormatRequest request) throws IdInvalidException;
    
    VariantFormatResponse getVariantFormatById(Long variantFormatId) throws IdInvalidException;
    
    VariantFormatResponse getVariantFormatByCode(String code) throws IdInvalidException;
    
    List<VariantFormatResponse> getAllVariantFormats();
    
    VariantFormatResponse updateVariantFormat(Long variantFormatId, UpdateVariantFormatRequest request) throws IdInvalidException;
    
    void deleteVariantFormat(Long variantFormatId) throws IdInvalidException;
}
