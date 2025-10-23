package com.practice.commerce.domain.category.controller.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record CreateCategoryRequest(
        @NotBlank
        @Size(min = 2, max = 16)
        String name,

        CategoryStatusRequest status,
        UUID parentId
) {
}
