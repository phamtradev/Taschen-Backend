package vn.edu.iuh.fit.bookstorebackend.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import vn.edu.iuh.fit.bookstorebackend.dto.request.CreateCategoryRequest;
import vn.edu.iuh.fit.bookstorebackend.dto.response.CategoryResponse;
import vn.edu.iuh.fit.bookstorebackend.model.Category;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "books", ignore = true)
    Category toCategory(CreateCategoryRequest request);

    CategoryResponse toCategoryResponse(Category category);
}
