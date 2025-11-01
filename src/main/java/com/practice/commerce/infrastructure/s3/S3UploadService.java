package com.practice.commerce.infrastructure.s3;

import com.practice.commerce.infrastructure.config.S3Properties;
import com.practice.commerce.domain.product.entity.MediaType;
import com.practice.commerce.domain.product.entity.Product;
import com.practice.commerce.domain.product.service.ProductMediaService;
import com.practice.commerce.infrastructure.s3.exception.FileUploadException;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.imageio.ImageIO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.Delete;
import software.amazon.awssdk.services.s3.model.DeleteObjectsRequest;
import software.amazon.awssdk.services.s3.model.ObjectIdentifier;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3UploadService {

    private final S3Client s3;
    private final S3Properties s3Properties;
    private final ProductMediaService productMediaService;
    private final S3ObjectUploader s3ObjectUploader;

    public void upload(Product product, List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            return;
        }

        List<String> uploadedKeys = new ArrayList<>();

        try {
            for (int pos = 0; pos < files.size(); pos++) {
                MultipartFile file = files.get(pos);

                String contentType = getContentType(file);
                String extension = getExtension(file, contentType);
                String objectKey = String.format(
                        "products/%s/images/%02d-%s.%s",
                        product.getId(), pos, UUID.randomUUID(), extension
                );

                // 메타데이터 추출 (width/height)
                ImageMeta meta = readImageMeta(file);
                s3ObjectUploader.uploadFile(objectKey, file, contentType);
                uploadedKeys.add(objectKey);

                // DB 저장
                productMediaService.create(
                        product, MediaType.IMAGE,
                        s3Properties.getBucket(), objectKey,
                        pos, meta.width(), meta.height()
                );
            }
        } catch (Exception ex) {
            deleteUploadedObjects(uploadedKeys); // 업로드된 이미지 삭제
            throw new FileUploadException("이미지 등록에 실패하여 상품 등록에 실패하였습니다.");
        }
    }

    private void deleteUploadedObjects(List<String> objectKeys) {
        if (objectKeys.isEmpty()) {
            return;
        }

        try {
            var objects = objectKeys.stream()
                    .map(objectKey -> ObjectIdentifier.builder().key(objectKey).build())
                    .toList();

            DeleteObjectsRequest deleteObjectsRequest = DeleteObjectsRequest.builder()
                    .bucket(s3Properties.getBucket())
                    .delete(
                            Delete.builder()
                                    .objects(objects)
                                    .build())
                    .build();

            s3.deleteObjects(deleteObjectsRequest);
            log.warn("업르도된 모든 객체 삭제 성공 : {}", objectKeys.size());
        } catch (Exception e) {
            log.error("업르도된 객체 삭제 실패: {}", objectKeys, e);
        }
    }

    private ImageMeta readImageMeta(MultipartFile file) throws IOException {
        try (var in = file.getInputStream()) {
            BufferedImage img = ImageIO.read(in);
            if (img == null) {
                throw new IOException("Unsupported image format");
            }
            return new ImageMeta(img.getWidth(), img.getHeight());
        }
    }

    private String getContentType(MultipartFile file) {
        return Optional.ofNullable(file.getContentType())
                .orElse("application/json");
    }

    private String getExtension(MultipartFile file, String contentType) {
        String ext = null;
        String name = file.getOriginalFilename();
        if (name != null && name.contains(".")) {
            ext = name.substring(name.lastIndexOf('.') + 1);
        } else if (contentType.startsWith("image/")) {
            ext = contentType.substring("image/".length());
        }
        return (ext == null || ext.isBlank()) ? "bin" : ext;
    }
}