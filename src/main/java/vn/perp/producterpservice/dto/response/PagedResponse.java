package vn.perp.producterpservice.dto.response;

import lombok.Builder;
import org.springframework.data.domain.Page;

import java.util.List;

@Builder
public record PagedResponse<T>(
        List<T> content,
        PageMeta page
) {

    @Builder
    public record PageMeta(
            int size,
            int number,
            long totalElements,
            int totalPages
    ) {
    }

    public static <T> PagedResponse<T> from(Page<T> page) {
        return PagedResponse.<T>builder()
                .content(page.getContent())
                .page(PageMeta.builder()
                        .size(page.getSize())
                        .number(page.getNumber())
                        .totalElements(page.getTotalElements())
                        .totalPages(page.getTotalPages())
                        .build())
                .build();
    }
}
