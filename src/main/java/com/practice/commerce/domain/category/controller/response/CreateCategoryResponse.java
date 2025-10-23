package com.practice.commerce.domain.category.controller.response;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class CreateCategoryResponse {

    @NotNull
    private UUID id;

    @NotBlank
    @Size(min = 2, max = 16)
    private String name;

    @NotNull
    private CategoryStatusResponse status;

    private UUID parentId;
}
