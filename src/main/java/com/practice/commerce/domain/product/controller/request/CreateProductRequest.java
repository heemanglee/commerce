package com.practice.commerce.domain.product.controller.request;

import com.practice.commerce.domain.product.entity.ProductStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record CreateProductRequest(
        @NotBlank
        @Size(max = 25)
        String name,

        @NotBlank
        String description,

        @NotNull
        UUID categoryId,

        @NotNull
        ProductStatus status
) {
}
