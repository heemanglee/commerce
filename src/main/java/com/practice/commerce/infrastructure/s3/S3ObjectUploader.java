package com.practice.commerce.infrastructure.s3;

import com.practice.commerce.infrastructure.config.S3Properties;
import com.practice.commerce.infrastructure.s3.exception.FileUploadException;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

@Slf4j
@RequiredArgsConstructor
@Service
public class S3ObjectUploader {

    private final S3Client s3Client;
    private final S3Properties s3Properties;

    @Retryable(
            value = {IOException.class, S3Exception.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 500, multiplier = 2)
    )
    public void uploadFile(String objectKey, MultipartFile file, String contentType) throws IOException {
        PutObjectRequest objectUploadRequest = PutObjectRequest.builder()
                .bucket(s3Properties.getBucket())
                .key(objectKey)
                .contentType(contentType)
                .build();

        try (var in = file.getInputStream()) {
            s3Client.putObject(objectUploadRequest, RequestBody.fromInputStream(in, file.getSize()));
        }
        log.info("S3 putObject success: {}", objectKey);
    }

    // 이미지 업로드 하나라도 실패할 시 상품 등록 취소
    @Recover
    public void recover(Exception e, String key, MultipartFile file, String contentType) {
        log.error("S3 putObject failed: {}", key, e);
        throw new FileUploadException("S3 putObject failed: " + key, e);
    }
}
