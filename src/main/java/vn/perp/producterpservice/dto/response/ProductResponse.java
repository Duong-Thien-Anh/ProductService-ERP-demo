package vn.perp.producterpservice.dto.response;

import lombok.Builder;
import vn.perp.producterpservice.model.ProductStatus;

import java.math.BigDecimal;
import java.util.UUID;

@Builder
public record ProductResponse(
        UUID id,
        String sku,
        String name,
        CategoryResponse category,
        BigDecimal price,
        String currency,
        Integer stockQuantity,
        ProductStatus status,
        String imageUrl
) {
}
