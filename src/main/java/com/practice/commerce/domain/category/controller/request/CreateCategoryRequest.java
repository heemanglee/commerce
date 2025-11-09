package com.practice.commerce.domain.category.controller.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record CreateCategoryRequest(
    @NotBlank(message = "카테고리 이름은 필수 값입니다.")
    @Size(min = 2, max = 16, message = "카테고리 이름은 최소 2자 이상, 최대 16 글자입니다.")
    String name,

    @NotNull(message = "카테고리의 상태(status)는 필수 값입니다.")
    CategoryStatusRequest status,

    UUID parentId
) {
}
