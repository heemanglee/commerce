package com.practice.commerce.domain.product.controller.request;

import java.util.List;
import java.util.UUID;

public record DeleteProductMediaRequest(
        List<DeleteMedia> medias
) {

    public record DeleteMedia(
            UUID id
    ) {
    }
}
