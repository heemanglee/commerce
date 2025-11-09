package com.practice.commerce.domain.product.controller.request;

import com.practice.commerce.domain.product.entity.ProductStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record CreateProductRequest(
        @NotBlank(message = "상품명은 필수 값입니다.")
        @Size(min = 8, max = 100, message = "상품명은 최소 8글자, 최대 100글자 이내이어야 합니다.")
        String name,

        @NotBlank(message = "상품 설명은 필수 값입니다.")
        String description,

        @NotNull(message = "상품의 카테고리는 필수 값입니다.")
        UUID categoryId,

        @NotNull(message = "상품의 상태는 필수 값입니다.")
        ProductStatus status
) {
}
