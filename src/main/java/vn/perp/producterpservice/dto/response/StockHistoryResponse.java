package vn.perp.producterpservice.dto.response;

import lombok.Builder;
import vn.perp.producterpservice.model.StockTransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record StockHistoryResponse(
        UUID id,
        StockTransactionType transactionType,
        Integer quantityChange,
        Integer previousQuantity,
        Integer newQuantity,
        BigDecimal unitCost,
        BigDecimal totalCost,
        String reason,
        UUID changedBy,
        LocalDateTime changedAt
) {
}
