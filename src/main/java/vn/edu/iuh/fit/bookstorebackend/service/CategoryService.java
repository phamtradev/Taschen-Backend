package vn.edu.iuh.fit.bookstorebackend.service;

import vn.edu.iuh.fit.bookstorebackend.dto.request.CreateCategoryRequest;
import vn.edu.iuh.fit.bookstorebackend.dto.request.UpdateCategoryRequest;
import vn.edu.iuh.fit.bookstorebackend.dto.response.CategoryResponse;
import vn.edu.iuh.fit.bookstorebackend.exception.IdInvalidException;

import java.util.List;

public interface CategoryService {
    
    CategoryResponse createCategory(CreateCategoryRequest request) throws IdInvalidException;
    
    CategoryResponse getCategoryById(Long categoryId) throws IdInvalidException;
    
    CategoryResponse getCategoryByCode(String code) throws IdInvalidException;
    
    List<CategoryResponse> getAllCategories();
    
    CategoryResponse updateCategory(Long categoryId, UpdateCategoryRequest request) throws IdInvalidException;
    
    void deleteCategory(Long categoryId) throws IdInvalidException;
}
