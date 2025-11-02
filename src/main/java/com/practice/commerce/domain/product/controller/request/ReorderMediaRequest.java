package com.practice.commerce.domain.product.controller.request;


import java.util.List;
import java.util.UUID;

public record ReorderMediaRequest(
        List<MediaPosition> mediaPositions
) {
    public record MediaPosition(
            UUID id,
            int position
    ) {
    }
}
