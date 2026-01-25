package vn.edu.iuh.fit.bookstorebackend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.iuh.fit.bookstorebackend.dto.request.CreateCategoryRequest;
import vn.edu.iuh.fit.bookstorebackend.dto.request.UpdateCategoryRequest;
import vn.edu.iuh.fit.bookstorebackend.dto.response.CategoryResponse;
import vn.edu.iuh.fit.bookstorebackend.exception.IdInvalidException;
import vn.edu.iuh.fit.bookstorebackend.service.CategoryService;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping
    public ResponseEntity<CategoryResponse> createCategory(
            @RequestBody CreateCategoryRequest request) throws IdInvalidException {
        CategoryResponse categoryResponse = categoryService.createCategory(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(categoryResponse);
    }

    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getAllCategories() {
        List<CategoryResponse> categories = categoryService.getAllCategories();
        return ResponseEntity.status(HttpStatus.OK).body(categories);
    }

    @GetMapping("/{categoryId}")
    public ResponseEntity<CategoryResponse> getCategoryById(
            @PathVariable Long categoryId) throws IdInvalidException {
        CategoryResponse categoryResponse = categoryService.getCategoryById(categoryId);
        return ResponseEntity.status(HttpStatus.OK).body(categoryResponse);
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<CategoryResponse> getCategoryByCode(
            @PathVariable String code) throws IdInvalidException {
        CategoryResponse categoryResponse = categoryService.getCategoryByCode(code);
        return ResponseEntity.status(HttpStatus.OK).body(categoryResponse);
    }

    @PutMapping("/{categoryId}")
    public ResponseEntity<CategoryResponse> updateCategory(
            @PathVariable Long categoryId,
            @RequestBody UpdateCategoryRequest request) throws IdInvalidException {
        CategoryResponse categoryResponse = categoryService.updateCategory(
                categoryId, request);
        return ResponseEntity.status(HttpStatus.OK).body(categoryResponse);
    }

    @DeleteMapping("/{categoryId}")
    public ResponseEntity<Void> deleteCategory(
            @PathVariable Long categoryId) throws IdInvalidException {
        categoryService.deleteCategory(categoryId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
