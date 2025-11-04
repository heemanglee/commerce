package com.practice.commerce.infrastructure.message;

import java.util.List;

public interface MessageQueueService {
    void sendS3DeletionMessage(S3DeletionMessage message);

    void sendS3DeletionMessages(List<S3DeletionMessage> messages);
}
