package vn.perp.producterpservice.dto.response;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record LowStockAlertResponse(
        ProductResponse product,
        Integer currentStock,
        Integer minimumStockLevel,
        Integer reorderPoint,
        Integer recommendedOrderQuantity,
        LocalDateTime alertCreatedAt
) {
}
