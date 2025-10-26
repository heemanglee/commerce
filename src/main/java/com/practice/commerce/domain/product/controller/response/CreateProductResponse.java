package com.practice.commerce.domain.product.controller.response;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CreateProductResponse {

    private UUID productId;
}
