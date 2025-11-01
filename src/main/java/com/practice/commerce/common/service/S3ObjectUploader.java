package com.practice.commerce.common.service;

import com.practice.commerce.common.config.S3Properties;
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

    // 모든 재시도 실패 시 호출되어 사위 트랜잭션 롤백 처리
    @Recover
    public void recover(Exception e, String key, MultipartFile file, String contentType) {
        log.error("S3 putObject failed: {}", key, e);
        throw new RuntimeException("S3 putObject failed: " + key, e);
    }
}
