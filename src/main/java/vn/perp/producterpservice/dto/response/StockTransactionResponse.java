package vn.perp.producterpservice.dto.response;

import lombok.Builder;

import java.util.UUID;

@Builder
public record StockTransactionResponse(
        UUID transactionId,
        UUID productId,
        Integer previousQuantity,
        Integer newQuantity
) {
}
