package com.practice.commerce.infrastructure.message;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class S3DeletionMessage {

    private UUID mediaId;
    private String bucketName;
    private String bucketKey;

    @Builder.Default
    private int retryCount = 0;

    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    public S3DeletionMessage incrementRetryCount() {
        return S3DeletionMessage.builder()
                .mediaId(this.mediaId)
                .bucketName(this.bucketName)
                .bucketKey(this.bucketKey)
                .retryCount(this.retryCount + 1)
                .timestamp(LocalDateTime.now())
                .build();
    }
}