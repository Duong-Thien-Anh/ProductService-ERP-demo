package vn.perp.producterpservice.dto.response;

import lombok.Builder;
import vn.perp.producterpservice.model.ProductStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Builder
public record ProductDetailResponse(
        UUID id,
        String sku,
        String barcode,
        String name,
        String description,
        CategoryResponse category,
        String brand,
        String manufacturer,
        BigDecimal price,
        BigDecimal costPrice,
        String currency,
        Integer stockQuantity,
        Integer minimumStockLevel,
        Integer maximumStockLevel,
        Integer reorderPoint,
        String unit,
        BigDecimal weight,
        String weightUnit,
        BigDecimal dimensionsLength,
        BigDecimal dimensionsWidth,
        BigDecimal dimensionsHeight,
        String dimensionUnit,
        BigDecimal taxRate,
        Boolean isTaxable,
        Boolean isActive,
        ProductStatus status,
        String imageUrl,
        List<String> additionalImages,
        List<String> tags,
        Integer warrantyPeriod,
        String warrantyDescription,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
