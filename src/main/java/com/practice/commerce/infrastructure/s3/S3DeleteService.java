package com.practice.commerce.infrastructure.s3;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.S3Exception;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3DeleteService {

    private final S3Client s3Client;

    @Retryable(
            retryFor = {S3Exception.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 3)
    )
    public boolean deleteFile(String bucketName, String key) {
        try {
            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            s3Client.deleteObject(deleteRequest);

            log.info("Successfully deleted file from S3: bucket={}, key={}", bucketName, key);
            return true;

        } catch (NoSuchKeyException e) {
            log.warn("File not found in S3 (already deleted): bucket={}, key={}", bucketName, key);
            return true;
        } catch (S3Exception e) {
            log.error("Failed to delete file from S3: bucket={}, key={}, errorCode={}",
                    bucketName, key, e.awsErrorDetails().errorCode(), e);
            throw e; // 재시도
        } catch (Exception e) {
            log.error("Unexpected error deleting file from S3: bucket={}, key={}", bucketName, key, e);
            return false;
        }
    }
}