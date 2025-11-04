package com.practice.commerce.infrastructure.message;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.practice.commerce.infrastructure.message.exception.MessageQueueException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;
import software.amazon.awssdk.services.sqs.model.SqsException;

@Slf4j
@Service
@RequiredArgsConstructor
public class SqsMessageQueueService implements MessageQueueService {

    private final SqsClient sqsClient;
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Value("${app.queue.sqs}")
    private String queueUrl;

    @Override
    public void sendS3DeletionMessage(S3DeletionMessage message) {
        try {
            String messageBody = objectMapper.writeValueAsString(message);

            SendMessageRequest request = SendMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .messageBody(messageBody)
                    .build();

            SendMessageResponse response = sqsClient.sendMessage(request);

            log.info("SQS로 메시지 전송: messageId={}, mediaId={}, bucketKey={}",
                    response.messageId(), message.getMediaId(), message.getBucketKey());
        } catch (JsonProcessingException e) {
            log.error("메시지 직렬화 실패: {}", message, e);
            throw new MessageQueueException("Failed to serialize SQS message", e);
        } catch (SqsException e) {
            log.error("SQS로 메시지 전송 실패: {}", message, e);
            throw new MessageQueueException("Failed to send SQS message", e);
        }
    }

    @Override
    public void sendS3DeletionMessages(List<S3DeletionMessage> messages) {
        if (messages == null || messages.isEmpty()) {
            return;
        }

        for (S3DeletionMessage message : messages) {
            sendS3DeletionMessage(message);
        }
    }
}