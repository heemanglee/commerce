package com.practice.commerce.domain.user.controller.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(
        @NotBlank
        @Email
        String email,

        @NotBlank
        @Size(min = 8, max = 16)
        String password
) {
}
