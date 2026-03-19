package vn.perp.producterpservice.dto.response;

import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;

@Builder
public record InventorySummaryResponse(
        long totalProducts,
        BigDecimal totalStockValue,
        long activeProducts,
        long lowStockProducts,
        long outOfStockProducts,
        long discontinuedProducts,
        List<CategoryBreakdown> categoryBreakdown
) {
    @Builder
    public record CategoryBreakdown(
            String category,
            long productCount,
            BigDecimal totalValue
    ) {
    }
}
