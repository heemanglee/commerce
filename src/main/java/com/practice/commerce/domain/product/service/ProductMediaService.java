package com.practice.commerce.domain.product.service;

import com.practice.commerce.domain.product.entity.MediaType;
import com.practice.commerce.domain.product.entity.Product;
import com.practice.commerce.domain.product.entity.ProductMedia;
import com.practice.commerce.domain.product.repository.ProductMediaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class ProductMediaService {

    private final ProductMediaRepository productMediaRepository;

    public void create(Product product, MediaType mediaType,
                       String bucketName, String objectKey, int pos,
                       int width, int height) {
        ProductMedia productMedia = ProductMedia.builder()
                .product(product)
                .mediaType(mediaType)
                .bucketName(bucketName)
                .bucketKey(objectKey)
                .position(pos)
                .width(width)
                .height(height)
                .build();
        productMediaRepository.save(productMedia);
    }
}