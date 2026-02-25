package vn.edu.iuh.fit.bookstorebackend.service.Impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.iuh.fit.bookstorebackend.dto.request.CreateVariantFormatRequest;
import vn.edu.iuh.fit.bookstorebackend.dto.request.UpdateVariantFormatRequest;
import vn.edu.iuh.fit.bookstorebackend.dto.response.VariantFormatResponse;
import vn.edu.iuh.fit.bookstorebackend.exception.IdInvalidException;
import vn.edu.iuh.fit.bookstorebackend.mapper.VariantFormatMapper;
import vn.edu.iuh.fit.bookstorebackend.model.VariantFormat;
import vn.edu.iuh.fit.bookstorebackend.repository.VariantFormatRepository;
import vn.edu.iuh.fit.bookstorebackend.service.VariantFormatService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VariantFormatServiceImpl implements VariantFormatService {

    private final VariantFormatRepository variantFormatRepository;
    private final VariantFormatMapper variantFormatMapper;

    @Override
    @Transactional
    public VariantFormatResponse createVariantFormat(CreateVariantFormatRequest request) throws IdInvalidException {
        validateRequest(request);
        validateVariantFormatCode(request.getCode());
        validateVariantFormatName(request.getName());
        validateCodeNotExists(request.getCode());
        
        VariantFormat variantFormat = createVariantFormatFromRequest(request);
        VariantFormat savedVariantFormat = variantFormatRepository.save(variantFormat);
        
        return variantFormatMapper.toVariantFormatResponse(savedVariantFormat);
    }
    
    private void validateRequest(CreateVariantFormatRequest request) throws IdInvalidException {
        if (request == null) {
            throw new IdInvalidException("CreateVariantFormatRequest cannot be null");
        }
    }
    
    private void validateVariantFormatCode(String code) throws IdInvalidException {
        if (code == null || code.trim().isEmpty()) {
            throw new IdInvalidException("Variant format code cannot be null or empty");
        }
    }
    
    private void validateVariantFormatName(String name) throws IdInvalidException {
        if (name == null || name.trim().isEmpty()) {
            throw new IdInvalidException("Variant format name cannot be null or empty");
        }
    }
    
    private void validateCodeNotExists(String code) {
        if (variantFormatRepository.existsByCode(code)) {
            throw new RuntimeException("Variant format with code already exists: " + code);
        }
    }
    
    private VariantFormat createVariantFormatFromRequest(CreateVariantFormatRequest request) {
        VariantFormat variantFormat = variantFormatMapper.toVariantFormat(request);
        variantFormat.setCode(request.getCode().trim());
        variantFormat.setName(request.getName().trim());
        return variantFormat;
    }

    @Override
    public VariantFormatResponse getVariantFormatById(Long variantFormatId) throws IdInvalidException {
        validateVariantFormatId(variantFormatId);
        VariantFormat variantFormat = findVariantFormatById(variantFormatId);
        return variantFormatMapper.toVariantFormatResponse(variantFormat);
    }

    @Override
    public VariantFormatResponse getVariantFormatByCode(String code) throws IdInvalidException {
        validateVariantFormatCode(code);
        VariantFormat variantFormat = findVariantFormatByCode(code);
        return variantFormatMapper.toVariantFormatResponse(variantFormat);
    }

    @Override
    public List<VariantFormatResponse> getAllVariantFormats() {
        List<VariantFormat> variantFormats = variantFormatRepository.findAll();
        return mapToVariantFormatResponseList(variantFormats);
    }
    
    private void validateVariantFormatId(Long variantFormatId) throws IdInvalidException {
        if (variantFormatId == null || variantFormatId <= 0) {
            throw new IdInvalidException("Variant format identifier is invalid: " + variantFormatId);
        }
    }
    
    private VariantFormat findVariantFormatById(Long variantFormatId) {
        return variantFormatRepository.findById(variantFormatId)
                .orElseThrow(() -> new RuntimeException("Variant format not found with identifier: " + variantFormatId));
    }
    
    private VariantFormat findVariantFormatByCode(String code) {
        return variantFormatRepository.findByCode(code)
                .orElseThrow(() -> new RuntimeException("Variant format not found with code: " + code));
    }

    @Override
    @Transactional
    public VariantFormatResponse updateVariantFormat(Long variantFormatId, UpdateVariantFormatRequest request) throws IdInvalidException {
        validateVariantFormatId(variantFormatId);
        validateRequest(request);
        
        VariantFormat variantFormat = findVariantFormatById(variantFormatId);
        updateVariantFormatFields(variantFormat, request);
        
        VariantFormat updatedVariantFormat = variantFormatRepository.save(variantFormat);
        return variantFormatMapper.toVariantFormatResponse(updatedVariantFormat);
    }
    
    private void validateRequest(UpdateVariantFormatRequest request) throws IdInvalidException {
        if (request == null) {
            throw new IdInvalidException("UpdateVariantFormatRequest cannot be null");
        }
    }
    
    private void updateVariantFormatFields(VariantFormat variantFormat, UpdateVariantFormatRequest request) {
        if (request.getCode() != null && !request.getCode().trim().isEmpty()) {
            validateCodeChange(variantFormat, request.getCode());
            variantFormat.setCode(request.getCode().trim());
        }
        
        if (request.getName() != null && !request.getName().trim().isEmpty()) {
            variantFormat.setName(request.getName().trim());
        }
    }
    
    private void validateCodeChange(VariantFormat variantFormat, String newCode) {
        if (!newCode.equals(variantFormat.getCode()) 
                && variantFormatRepository.existsByCode(newCode)) {
            throw new RuntimeException("Variant format with code already exists: " + newCode);
        }
    }

    @Override
    @Transactional
    public void deleteVariantFormat(Long variantFormatId) throws IdInvalidException {
        validateVariantFormatId(variantFormatId);
        VariantFormat variantFormat = findVariantFormatById(variantFormatId);
        
        if (variantFormat.getVariants() != null && !variantFormat.getVariants().isEmpty()) {
            throw new IdInvalidException("Cannot delete variant format because it has " 
                + variantFormat.getVariants().size() + " associated variant(s)");
        }
        
        variantFormatRepository.delete(variantFormat);
    }

    private List<VariantFormatResponse> mapToVariantFormatResponseList(List<VariantFormat> variantFormats) {
        return variantFormats.stream()
                .map(variantFormatMapper::toVariantFormatResponse)
                .collect(Collectors.toList());
    }
}
