package com.practice.commerce.domain.product.service;

import com.practice.commerce.common.config.S3Properties;
import com.practice.commerce.domain.product.entity.MediaType;
import com.practice.commerce.domain.product.entity.Product;
import com.practice.commerce.domain.product.entity.ProductMedia;
import com.practice.commerce.domain.product.repository.ProductMediaRepository;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.imageio.ImageIO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@RequiredArgsConstructor
@Service
public class ProductMediaService {

    private final ProductMediaRepository productMediaRepository;
    private final S3Client s3Client;
    private final S3Properties s3Properties;

    public void uploadImages(Product product, List<MultipartFile> files) throws IOException {
        if (files == null || files.isEmpty()) {
            return;
        }

        List<UUID> uploadKeys = new ArrayList<>();
        for (int i = 0; i < files.size(); i++) {
            MultipartFile file = files.get(i);

            // 이미지 크기 읽기
            BufferedImage image = ImageIO.read(file.getInputStream());
            int width = image.getWidth();
            int height = image.getHeight();

            // S3 업로드 (파일 바이트를 직접 사용)
            String key = String.format("products/%s/images/%s", product.getId(), UUID.randomUUID());
            PutObjectRequest bucketObject = PutObjectRequest.builder()
                    .bucket(s3Properties.getBucket())
                    .key(key)
                    .contentType(file.getContentType())
                    .build();
            s3Client.putObject(
                    bucketObject,
                    RequestBody.fromBytes(file.getBytes())
            );

            ProductMedia productMedia = ProductMedia.builder()
                    .product(product)
                    .mediaType(MediaType.IMAGE)
                    .bucketName(s3Properties.getBucket())
                    .bucketKey(key)
                    .position(i)
                    .width(width)
                    .height(height)
                    .build();
            productMediaRepository.save(productMedia);
        }
    }
}