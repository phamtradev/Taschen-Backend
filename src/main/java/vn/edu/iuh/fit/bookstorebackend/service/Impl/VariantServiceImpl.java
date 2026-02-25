package vn.edu.iuh.fit.bookstorebackend.service.Impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.iuh.fit.bookstorebackend.dto.request.CreateVariantRequest;
import vn.edu.iuh.fit.bookstorebackend.dto.request.UpdateVariantRequest;
import vn.edu.iuh.fit.bookstorebackend.dto.response.VariantResponse;
import vn.edu.iuh.fit.bookstorebackend.exception.IdInvalidException;
import vn.edu.iuh.fit.bookstorebackend.model.Book;
import vn.edu.iuh.fit.bookstorebackend.model.Variant;
import vn.edu.iuh.fit.bookstorebackend.model.VariantFormat;
import vn.edu.iuh.fit.bookstorebackend.mapper.VariantMapper;
import vn.edu.iuh.fit.bookstorebackend.repository.BookRepository;
import vn.edu.iuh.fit.bookstorebackend.repository.VariantFormatRepository;
import vn.edu.iuh.fit.bookstorebackend.repository.VariantRepository;
import vn.edu.iuh.fit.bookstorebackend.service.VariantService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VariantServiceImpl implements VariantService {

    private final VariantRepository variantRepository;
    private final BookRepository bookRepository;
    private final VariantFormatRepository variantFormatRepository;
    private final VariantMapper variantMapper;

    @Override
    @Transactional
    public VariantResponse createVariant(CreateVariantRequest request) throws IdInvalidException {
        validateRequest(request);
        validateBookId(request.getBookId());
        validateVariantFormatId(request.getVariantFormatId());
        
        Book book = findBookById(request.getBookId());
        VariantFormat variantFormat = findVariantFormatById(request.getVariantFormatId());
        Variant variant = createVariantFromRequest(request, book, variantFormat);
        
        Variant savedVariant = variantRepository.save(variant);
        return variantMapper.toVariantResponse(savedVariant);
    }
    
    private void validateRequest(CreateVariantRequest request) throws IdInvalidException {
        if (request == null) {
            throw new IdInvalidException("CreateVariantRequest cannot be null");
        }
    }
    
    private void validateBookId(Long bookId) throws IdInvalidException {
        if (bookId == null || bookId <= 0) {
            throw new IdInvalidException("Book identifier is invalid: " + bookId);
        }
    }
    
    private void validateVariantFormatId(Long variantFormatId) throws IdInvalidException {
        if (variantFormatId == null || variantFormatId <= 0) {
            throw new IdInvalidException("Variant format identifier is invalid: " + variantFormatId);
        }
    }
    
    private Book findBookById(Long bookId) {
        return bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found with identifier: " + bookId));
    }
    
    private VariantFormat findVariantFormatById(Long variantFormatId) {
        return variantFormatRepository.findById(variantFormatId)
                .orElseThrow(() -> new RuntimeException("Variant format not found with identifier: " + variantFormatId));
    }
    
    private Variant createVariantFromRequest(CreateVariantRequest request, Book book, VariantFormat variantFormat) {
        Variant variant = new Variant();
        variant.setVariantFormat(variantFormat);
        variant.setBook(book);
        return variant;
    }

    @Override
    public VariantResponse getVariantById(Long variantId) throws IdInvalidException {
        validateVariantId(variantId);
        Variant variant = findVariantById(variantId);
        return variantMapper.toVariantResponse(variant);
    }

    @Override
    public List<VariantResponse> getAllVariants() {
        List<Variant> variants = variantRepository.findAll();
        return mapToVariantResponseList(variants);
    }

    @Override
    public List<VariantResponse> getVariantsByBookId(Long bookId) throws IdInvalidException {
        validateBookId(bookId);
        Book book = findBookById(bookId);
        
        List<Variant> variants = variantRepository.findByBook(book);
        return mapToVariantResponseList(variants);
    }
    
    private void validateVariantId(Long variantId) throws IdInvalidException {
        if (variantId == null || variantId <= 0) {
            throw new IdInvalidException("Variant identifier is invalid: " + variantId);
        }
    }
    
    private Variant findVariantById(Long variantId) {
        return variantRepository.findById(variantId)
                .orElseThrow(() -> new RuntimeException("Variant not found with identifier: " + variantId));
    }

    @Override
    @Transactional
    public VariantResponse updateVariant(Long variantId, UpdateVariantRequest request) throws IdInvalidException {
        validateVariantId(variantId);
        validateRequest(request);
        
        Variant variant = findVariantById(variantId);
        updateVariantFields(variant, request);
        
        Variant updatedVariant = variantRepository.save(variant);
        return variantMapper.toVariantResponse(updatedVariant);
    }
    
    private void validateRequest(UpdateVariantRequest request) throws IdInvalidException {
        if (request == null) {
            throw new IdInvalidException("UpdateVariantRequest cannot be null");
        }
    }
    
    private void updateVariantFields(Variant variant, UpdateVariantRequest request) {
        if (request.getVariantFormatId() != null && request.getVariantFormatId() > 0) {
            VariantFormat variantFormat = findVariantFormatById(request.getVariantFormatId());
            variant.setVariantFormat(variantFormat);
        }
        
        if (request.getBookId() != null && request.getBookId() > 0) {
            Book book = findBookById(request.getBookId());
            variant.setBook(book);
        }
    }

    @Override
    @Transactional
    public void deleteVariant(Long variantId) throws IdInvalidException {
        validateVariantId(variantId);
        Variant variant = findVariantById(variantId);
        variantRepository.delete(variant);
    }

    private List<VariantResponse> mapToVariantResponseList(List<Variant> variants) {
        return variants.stream()
                .map(variantMapper::toVariantResponse)
                .collect(Collectors.toList());
    }
}
