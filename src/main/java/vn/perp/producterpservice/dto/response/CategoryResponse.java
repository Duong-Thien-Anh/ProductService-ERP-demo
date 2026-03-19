package vn.perp.producterpservice.dto.response;

import lombok.Builder;

import java.util.UUID;

@Builder
public record CategoryResponse(
        UUID id,
        String name,
        String code,
        String description,
        UUID parentCategoryId,
        String parentCategoryName,
        boolean isActive,
        int displayOrder,
        long productCount
) {
}
