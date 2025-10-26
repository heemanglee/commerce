package com.practice.commerce.domain.product.controller.response;

import com.practice.commerce.domain.product.entity.Product;
import com.practice.commerce.domain.product.entity.ProductStatus;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GetProductResponse {

    private UUID productId;
    private String name;
    private String description;
    private UUID sellerId;
    private ProductStatus status;
    private Instant deletedAt;

    public static GetProductResponse of(Product product) {
        return new GetProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getSeller().getId(),
                product.getStatus(),
                product.getDeletedAt()
        );
    }
}
