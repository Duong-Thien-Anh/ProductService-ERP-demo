package vn.perp.producterpservice.dto.request;

import jakarta.validation.constraints.NotNull;
import vn.perp.producterpservice.model.StockTransactionType;

import java.math.BigDecimal;

public record StockAdjustmentRequest(
        @NotNull Integer quantityChange,
        @NotNull StockTransactionType transactionType,
        String reason,
        BigDecimal unitCost,
        String referenceType,
        String referenceId,
        String location,
        String notes
) {
}
