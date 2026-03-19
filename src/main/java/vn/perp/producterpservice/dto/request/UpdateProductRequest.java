package vn.perp.producterpservice.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import vn.perp.producterpservice.model.ProductStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record UpdateProductRequest(
        @Size(min = 3, max = 200) String name,
        @Size(max = 100) String barcode,
        String description,
        UUID categoryId,
        @Size(max = 100) String brand,
        @Size(max = 100) String manufacturer,
        @DecimalMin(value = "0.0", inclusive = false) BigDecimal price,
        @DecimalMin(value = "0.0", inclusive = false) BigDecimal costPrice,
        @Size(min = 3, max = 3) String currency,
        @PositiveOrZero Integer stockQuantity,
        @PositiveOrZero Integer minimumStockLevel,
        @PositiveOrZero Integer maximumStockLevel,
        @PositiveOrZero Integer reorderPoint,
        @Size(max = 20) String unit,
        @PositiveOrZero BigDecimal weight,
        @Size(max = 10) String weightUnit,
        @PositiveOrZero BigDecimal dimensionsLength,
        @PositiveOrZero BigDecimal dimensionsWidth,
        @PositiveOrZero BigDecimal dimensionsHeight,
        @Size(max = 10) String dimensionUnit,
        @PositiveOrZero BigDecimal taxRate,
        Boolean isTaxable,
        Boolean isActive,
        ProductStatus status,
        String imageUrl,
        List<String> additionalImages,
        List<String> tags,
        @Positive Integer warrantyPeriod,
        String warrantyDescription
) {
}
