package com.practice.commerce.domain.product.service;

import com.practice.commerce.domain.product.entity.MediaType;
import com.practice.commerce.domain.product.entity.Product;
import com.practice.commerce.domain.product.entity.ProductMedia;
import com.practice.commerce.domain.product.repository.ProductMediaRepository;
import com.practice.commerce.infrastructure.message.MessageQueueService;
import com.practice.commerce.infrastructure.message.S3DeletionMessage;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class ProductMediaService {

    private final ProductMediaRepository productMediaRepository;
    private final MessageQueueService messageQueueService;

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

    @Transactional
    public void deleteProductMedias(Product product) {
        List<ProductMedia> productMedias = productMediaRepository.findByProductId(product.getId());
        productMedias.forEach(ProductMedia::markAsDeleted);

        List<S3DeletionMessage> deleteMessages = productMedias.stream()
                .map(media -> S3DeletionMessage.builder()
                        .mediaId(media.getId())
                        .bucketKey(media.getBucketObjectKey())
                        .bucketName(media.getBucketName())
                        .retryCount(0)
                        .build()
                )
                .toList();

        messageQueueService.sendS3DeletionMessages(deleteMessages);
    }

    public List<S3DeletionMessage> getProductDeleteMessages(Product product) {
        List<ProductMedia> productMedias = productMediaRepository.findByProductId(product.getId());
        return productMedias.stream()
                .map(media -> S3DeletionMessage.builder()
                        .mediaId(media.getId())
                        .bucketKey(media.getBucketObjectKey())
                        .bucketName(media.getBucketName())
                        .retryCount(0)
                        .build()
                )
                .toList();
    }
}