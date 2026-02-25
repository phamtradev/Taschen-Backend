package vn.edu.iuh.fit.bookstorebackend.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import vn.edu.iuh.fit.bookstorebackend.dto.request.CreateBookRequest;
import vn.edu.iuh.fit.bookstorebackend.dto.response.BookResponse;
import vn.edu.iuh.fit.bookstorebackend.model.Book;
import vn.edu.iuh.fit.bookstorebackend.model.Category;
import vn.edu.iuh.fit.bookstorebackend.model.Supplier;
import vn.edu.iuh.fit.bookstorebackend.model.Variant;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface BookMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "variants", ignore = true)
    @Mapping(target = "categories", ignore = true)
    @Mapping(target = "price", ignore = true)
    @Mapping(target = "stockQuantity", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "supplier", ignore = true)
    @Mapping(target = "batches", ignore = true)
    Book toBook(CreateBookRequest request);

    @Mapping(target = "variantFormats", source = "variants", qualifiedByName = "variantsToFormats")
    @Mapping(target = "categoryIds", source = "categories", qualifiedByName = "categoriesToIds")
    @Mapping(target = "supplierId", source = "supplier", qualifiedByName = "supplierToId")
    BookResponse toBookResponse(Book book);

    @Named("variantsToFormats")
    default List<String> variantsToFormats(List<Variant> variants) {
        if (variants == null || variants.isEmpty()) {
            return new ArrayList<>();
        }
        return variants.stream()
                .map(Variant::getFormat)
                .filter(format -> format != null && !format.trim().isEmpty())
                .collect(Collectors.toList());
    }

    @Named("categoriesToIds")
    default List<Long> categoriesToIds(List<Category> categories) {
        if (categories == null || categories.isEmpty()) {
            return new ArrayList<>();
        }
        try {
            return categories.stream()
                    .map(Category::getId)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    @Named("supplierToId")
    default Long supplierToId(Supplier supplier) {
        return supplier != null ? supplier.getId() : null;
    }
}
