package vn.perp.producterpservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateCategoryRequest(
        @NotBlank @Size(max = 100) String name,
        @NotBlank @Size(max = 20) String code,
        String description,
        UUID parentCategoryId,
        String imageUrl,
        @PositiveOrZero Integer displayOrder
) {
}
