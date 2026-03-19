package vn.perp.producterpservice.dto.response;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Builder
public record InventoryValuationResponse(
        LocalDateTime reportDate,
        BigDecimal totalInventoryValue,
        List<InventoryValuationItemResponse> products
) {
}
