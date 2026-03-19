package vn.perp.producterpservice.dto.response;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record InventoryValuationItemResponse(
        String sku,
        String name,
        Integer quantity,
        BigDecimal costPrice,
        BigDecimal totalValue
) {
}
