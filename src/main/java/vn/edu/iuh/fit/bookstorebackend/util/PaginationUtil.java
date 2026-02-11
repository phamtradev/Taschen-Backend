package vn.edu.iuh.fit.bookstorebackend.util;

import org.springframework.data.domain.Page;
import vn.edu.iuh.fit.bookstorebackend.dto.response.PageResponse;

public class PaginationUtil {

    private PaginationUtil() {
    }

    public static <T> PageResponse<T> toPageResponse(Page<T> page) {
        PageResponse.Meta meta = PageResponse.Meta.builder()
                .page(page.getNumber() + 1)
                .pageSize(page.getSize())
                .pages(page.getTotalPages())
                .total(page.getTotalElements())
                .build();

        return PageResponse.<T>builder()
                .meta(meta)
                .result(page.getContent())
                .build();
    }
}
