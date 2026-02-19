package vn.edu.iuh.fit.bookstorebackend.service.Impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.iuh.fit.bookstorebackend.dto.request.CreateCategoryRequest;
import vn.edu.iuh.fit.bookstorebackend.dto.request.UpdateCategoryRequest;
import vn.edu.iuh.fit.bookstorebackend.dto.response.CategoryResponse;
import vn.edu.iuh.fit.bookstorebackend.exception.IdInvalidException;
import vn.edu.iuh.fit.bookstorebackend.model.Category;
import vn.edu.iuh.fit.bookstorebackend.mapper.CategoryMapper;
import vn.edu.iuh.fit.bookstorebackend.repository.CategoryRepository;
import vn.edu.iuh.fit.bookstorebackend.service.CategoryService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Override
    @Transactional
    public CategoryResponse createCategory(CreateCategoryRequest request) throws IdInvalidException {
        validateRequest(request);
        validateCategoryCode(request.getCode());
        validateCategoryName(request.getName());
        validateCodeNotExists(request.getCode());
        
        Category category = createCategoryFromRequest(request);
        Category savedCategory = categoryRepository.save(category);
        
        return categoryMapper.toCategoryResponse(savedCategory);
    }
    
    private void validateRequest(CreateCategoryRequest request) throws IdInvalidException {
        if (request == null) {
            throw new IdInvalidException("CreateCategoryRequest cannot be null");
        }
    }
    
    private void validateCategoryCode(String code) throws IdInvalidException {
        if (code == null || code.trim().isEmpty()) {
            throw new IdInvalidException("Category code cannot be null or empty");
        }
    }
    
    private void validateCategoryName(String name) throws IdInvalidException {
        if (name == null || name.trim().isEmpty()) {
            throw new IdInvalidException("Category name cannot be null or empty");
        }
    }
    
    private void validateCodeNotExists(String code) {
        if (categoryRepository.existsByCode(code)) {
            throw new RuntimeException("Category with code already exists: " + code);
        }
    }
    
    private Category createCategoryFromRequest(CreateCategoryRequest request) {
        Category category = categoryMapper.toCategory(request);
        category.setCode(request.getCode().trim());
        category.setName(request.getName().trim());
        return category;
    }

    @Override
    public CategoryResponse getCategoryById(Long categoryId) throws IdInvalidException {
        validateCategoryId(categoryId);
        Category category = findCategoryById(categoryId);
        return categoryMapper.toCategoryResponse(category);
    }

    @Override
    public CategoryResponse getCategoryByCode(String code) throws IdInvalidException {
        validateCategoryCode(code);
        Category category = findCategoryByCode(code);
        return categoryMapper.toCategoryResponse(category);
    }

    @Override
    public List<CategoryResponse> getAllCategories() {
        List<Category> categories = categoryRepository.findAll();
        return mapToCategoryResponseList(categories);
    }
    
    private void validateCategoryId(Long categoryId) throws IdInvalidException {
        if (categoryId == null || categoryId <= 0) {
            throw new IdInvalidException("Category identifier is invalid: " + categoryId);
        }
    }
    
    private Category findCategoryById(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found with identifier: " + categoryId));
    }
    
    private Category findCategoryByCode(String code) {
        return categoryRepository.findByCode(code)
                .orElseThrow(() -> new RuntimeException("Category not found with code: " + code));
    }

    @Override
    @Transactional
    public CategoryResponse updateCategory(Long categoryId, UpdateCategoryRequest request) throws IdInvalidException {
        validateCategoryId(categoryId);
        validateRequest(request);
        
        Category category = findCategoryById(categoryId);
        updateCategoryFields(category, request);
        
        Category updatedCategory = categoryRepository.save(category);
        return categoryMapper.toCategoryResponse(updatedCategory);
    }
    
    private void validateRequest(UpdateCategoryRequest request) throws IdInvalidException {
        if (request == null) {
            throw new IdInvalidException("UpdateCategoryRequest cannot be null");
        }
    }
    
    private void updateCategoryFields(Category category, UpdateCategoryRequest request) {
        if (request.getCode() != null && !request.getCode().trim().isEmpty()) {
            validateCodeChange(category, request.getCode());
            category.setCode(request.getCode().trim());
        }
        
        if (request.getName() != null && !request.getName().trim().isEmpty()) {
            category.setName(request.getName().trim());
        }
    }
    
    private void validateCodeChange(Category category, String newCode) {
        if (!newCode.equals(category.getCode()) 
                && categoryRepository.existsByCode(newCode)) {
            throw new RuntimeException("Category with code already exists: " + newCode);
        }
    }

    @Override
    @Transactional
    public void deleteCategory(Long categoryId) throws IdInvalidException {
        validateCategoryId(categoryId);
        Category category = findCategoryById(categoryId);
        
        if (category.getBooks() != null && !category.getBooks().isEmpty()) {
            throw new IdInvalidException("Cannot delete category because it has " 
                + category.getBooks().size() + " associated book(s)");
        }
        
        categoryRepository.delete(category);
    }

    private List<CategoryResponse> mapToCategoryResponseList(List<Category> categories) {
        return categories.stream()
                .map(categoryMapper::toCategoryResponse)
                .collect(Collectors.toList());
    }
}
