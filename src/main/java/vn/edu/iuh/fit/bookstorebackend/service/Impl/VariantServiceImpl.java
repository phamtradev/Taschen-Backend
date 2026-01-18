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
import vn.edu.iuh.fit.bookstorebackend.repository.BookRepository;
import vn.edu.iuh.fit.bookstorebackend.repository.VariantRepository;
import vn.edu.iuh.fit.bookstorebackend.service.VariantService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VariantServiceImpl implements VariantService {

    private final VariantRepository variantRepository;
    private final BookRepository bookRepository;

    @Override
    @Transactional
    public VariantResponse createVariant(CreateVariantRequest request) throws IdInvalidException {
        if (request == null) {
            throw new IdInvalidException("CreateVariantRequest cannot be null");
        }

        if (request.getBookId() == null || request.getBookId() <= 0) {
            throw new IdInvalidException("Book identifier is invalid: " + request.getBookId());
        }

        if (request.getFormat() == null || request.getFormat().trim().isEmpty()) {
            throw new IdInvalidException("Format cannot be null or empty");
        }

        Book book = bookRepository.findById(request.getBookId())
                .orElseThrow(() -> new RuntimeException("Book not found with identifier: " + request.getBookId()));

        Variant variant = new Variant();
        variant.setFormat(request.getFormat());
        variant.setBook(book);

        Variant savedVariant = variantRepository.save(variant);
        return convertToVariantResponse(savedVariant);
    }

    @Override
    public VariantResponse getVariantById(Long variantId) throws IdInvalidException {
        if (variantId == null || variantId <= 0) {
            throw new IdInvalidException("Variant identifier is invalid: " + variantId);
        }

        Variant variant = variantRepository.findById(variantId)
                .orElseThrow(() -> new RuntimeException("Variant not found with identifier: " + variantId));
        return convertToVariantResponse(variant);
    }

    @Override
    public List<VariantResponse> getAllVariants() {
        List<Variant> variants = variantRepository.findAll();
        return variants.stream()
                .map(this::convertToVariantResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<VariantResponse> getVariantsByBookId(Long bookId) throws IdInvalidException {
        if (bookId == null || bookId <= 0) {
            throw new IdInvalidException("Book identifier is invalid: " + bookId);
        }

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found with identifier: " + bookId));

        List<Variant> variants = variantRepository.findByBook(book);
        return variants.stream()
                .map(this::convertToVariantResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public VariantResponse updateVariant(Long variantId, UpdateVariantRequest request) throws IdInvalidException {
        if (variantId == null || variantId <= 0) {
            throw new IdInvalidException("Variant identifier is invalid: " + variantId);
        }

        if (request == null) {
            throw new IdInvalidException("UpdateVariantRequest cannot be null");
        }

        Variant variant = variantRepository.findById(variantId)
                .orElseThrow(() -> new RuntimeException("Variant not found with identifier: " + variantId));

        if (request.getFormat() != null && !request.getFormat().trim().isEmpty()) {
            variant.setFormat(request.getFormat());
        }

        if (request.getBookId() != null && request.getBookId() > 0) {
            Book book = bookRepository.findById(request.getBookId())
                    .orElseThrow(() -> new RuntimeException("Book not found with identifier: " + request.getBookId()));
            variant.setBook(book);
        }

        Variant updatedVariant = variantRepository.save(variant);
        return convertToVariantResponse(updatedVariant);
    }

    @Override
    @Transactional
    public void deleteVariant(Long variantId) throws IdInvalidException {
        if (variantId == null || variantId <= 0) {
            throw new IdInvalidException("Variant identifier is invalid: " + variantId);
        }

        Variant variant = variantRepository.findById(variantId)
                .orElseThrow(() -> new RuntimeException("Variant not found with identifier: " + variantId));

        variantRepository.delete(variant);
    }

    private VariantResponse convertToVariantResponse(Variant variant) {
        VariantResponse variantResponse = new VariantResponse();
        variantResponse.setId(variant.getId());
        variantResponse.setFormat(variant.getFormat());
        variantResponse.setBookId(variant.getBook().getId());
        variantResponse.setBookTitle(variant.getBook().getTitle());
        return variantResponse;
    }
}
