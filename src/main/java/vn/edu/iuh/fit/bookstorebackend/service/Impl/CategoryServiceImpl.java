package vn.edu.iuh.fit.bookstorebackend.service.Impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.iuh.fit.bookstorebackend.dto.request.CreateCategoryRequest;
import vn.edu.iuh.fit.bookstorebackend.dto.request.UpdateCategoryRequest;
import vn.edu.iuh.fit.bookstorebackend.dto.response.CategoryResponse;
import vn.edu.iuh.fit.bookstorebackend.exception.IdInvalidException;
import vn.edu.iuh.fit.bookstorebackend.model.Category;
import vn.edu.iuh.fit.bookstorebackend.repository.CategoryRepository;
import vn.edu.iuh.fit.bookstorebackend.service.CategoryService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    @Transactional
    public CategoryResponse createCategory(CreateCategoryRequest request) throws IdInvalidException {
        if (request == null) {
            throw new IdInvalidException("CreateCategoryRequest cannot be null");
        }

        if (request.getCode() == null || request.getCode().trim().isEmpty()) {
            throw new IdInvalidException("Category code cannot be null or empty");
        }

        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new IdInvalidException("Category name cannot be null or empty");
        }

        // Check if code already exists
        if (categoryRepository.existsByCode(request.getCode())) {
            throw new RuntimeException("Category with code already exists: " + request.getCode());
        }

        Category category = new Category();
        category.setCode(request.getCode().trim());
        category.setName(request.getName().trim());

        Category savedCategory = categoryRepository.save(category);
        return convertToCategoryResponse(savedCategory);
    }

    @Override
    public CategoryResponse getCategoryById(Long categoryId) throws IdInvalidException {
        if (categoryId == null || categoryId <= 0) {
            throw new IdInvalidException("Category identifier is invalid: " + categoryId);
        }

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found with identifier: " + categoryId));
        return convertToCategoryResponse(category);
    }

    @Override
    public CategoryResponse getCategoryByCode(String code) throws IdInvalidException {
        if (code == null || code.trim().isEmpty()) {
            throw new IdInvalidException("Category code cannot be null or empty");
        }

        Category category = categoryRepository.findByCode(code)
                .orElseThrow(() -> new RuntimeException("Category not found with code: " + code));
        return convertToCategoryResponse(category);
    }

    @Override
    public List<CategoryResponse> getAllCategories() {
        List<Category> categories = categoryRepository.findAll();
        return categories.stream()
                .map(this::convertToCategoryResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CategoryResponse updateCategory(Long categoryId, UpdateCategoryRequest request) throws IdInvalidException {
        if (categoryId == null || categoryId <= 0) {
            throw new IdInvalidException("Category identifier is invalid: " + categoryId);
        }

        if (request == null) {
            throw new IdInvalidException("UpdateCategoryRequest cannot be null");
        }

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found with identifier: " + categoryId));

        // Check if code is being changed and if new code already exists for another category
        if (request.getCode() != null && !request.getCode().trim().isEmpty()) {
            if (!request.getCode().equals(category.getCode()) && categoryRepository.existsByCode(request.getCode())) {
                throw new RuntimeException("Category with code already exists: " + request.getCode());
            }
            category.setCode(request.getCode().trim());
        }

        if (request.getName() != null && !request.getName().trim().isEmpty()) {
            category.setName(request.getName().trim());
        }

        Category updatedCategory = categoryRepository.save(category);
        return convertToCategoryResponse(updatedCategory);
    }

    @Override
    @Transactional
    public void deleteCategory(Long categoryId) throws IdInvalidException {
        if (categoryId == null || categoryId <= 0) {
            throw new IdInvalidException("Category identifier is invalid: " + categoryId);
        }

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found with identifier: " + categoryId));

        categoryRepository.delete(category);
    }

    private CategoryResponse convertToCategoryResponse(Category category) {
        CategoryResponse categoryResponse = new CategoryResponse();
        categoryResponse.setId(category.getId());
        categoryResponse.setCode(category.getCode());
        categoryResponse.setName(category.getName());
        return categoryResponse;
    }
}
