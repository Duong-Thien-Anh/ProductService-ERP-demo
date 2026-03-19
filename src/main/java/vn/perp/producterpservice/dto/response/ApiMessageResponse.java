package vn.perp.producterpservice.dto.response;

import lombok.Builder;

import java.util.UUID;

@Builder
public record ApiMessageResponse(
        String message,
        UUID id
) {
}
