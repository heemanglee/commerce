package com.practice.commerce.infrastructure.message;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.practice.commerce.domain.product.repository.ProductMediaRepository;
import com.practice.commerce.infrastructure.s3.S3DeleteService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageResponse;

@Slf4j
@Component
@RequiredArgsConstructor
public class S3DeletionMessageConsumer {

    private final SqsClient sqsClient;
    private final S3DeleteService s3DeleteService;
    private final ProductMediaRepository productMediaRepository;
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Value("${app.queue.sqs}")
    private String queueUrl;

    @Value("${app.queue.dlq}")
    private String dlqUrl;

    private static final int MAX_MESSAGES = 10;
    private static final int WAIT_TIME_SECONDS = 10;
    private static final int MAX_RETRY_COUNT = 3;

    /**
     * SQS 메시지를 주기적으로 polling하여 S3 삭제 작업 수행 매 10초마다 실행
     */
    @Scheduled(fixedDelay = 10000)
    public void consumeMessages() {
        try {
            ReceiveMessageRequest receiveRequest = ReceiveMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .maxNumberOfMessages(MAX_MESSAGES)
                    .waitTimeSeconds(WAIT_TIME_SECONDS)
                    .build();

            ReceiveMessageResponse response = sqsClient.receiveMessage(receiveRequest);
            List<Message> messages = response.messages();

            if (messages.isEmpty()) {
                return;
            }

            for (Message message : messages) {
                processMessage(message);
            }

        } catch (Exception e) {
            log.error("SQS에 존재하는 메시지 처리 실패", e);
        }
    }

    private void processMessage(Message message) {
        try {
            // 메시지 파싱
            S3DeletionMessage deletionMessage = objectMapper.readValue(
                    message.body(),
                    S3DeletionMessage.class
            );

            log.info("SQS 메시지 삭제: mediaId={}, bucketKey={}",
                    deletionMessage.getMediaId(), deletionMessage.getBucketKey());

            boolean deleted = s3DeleteService.deleteFile(
                    deletionMessage.getBucketName(),
                    deletionMessage.getBucketKey()
            );

            if (deleted) {
                // S3 삭제 성공 시 DB에서 hard delete
                productMediaRepository.deleteById(deletionMessage.getMediaId());
                log.info("S3 Object 삭제 완료: mediaId={}, bucketKey={}",
                        deletionMessage.getMediaId(), deletionMessage.getBucketKey());

                deleteMessageFromQueue(message);
            } else {
                handleDeletionFailure(message, deletionMessage);
            }

        } catch (Exception e) {
            log.error("메시지를 다시 큐로 반환: {}", message.body(), e);
        }
    }

    private void handleDeletionFailure(Message message, S3DeletionMessage deletionMessage) {
        int currentRetryCount = deletionMessage.getRetryCount();

        if (currentRetryCount >= MAX_RETRY_COUNT) {
            log.error("SQS 메시지 삭제 최대 시도 횟수 실패: mediaId={}", deletionMessage.getMediaId());
            deleteMessageFromQueue(message);
        } else {
            log.warn("SQS 메시지 삭제 재시도: mediaId={}, retryCount={}",
                    deletionMessage.getMediaId(), currentRetryCount);
        }
    }

    private void deleteMessageFromQueue(Message message) {
        try {
            DeleteMessageRequest deleteRequest = DeleteMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .receiptHandle(message.receiptHandle())
                    .build();

            sqsClient.deleteMessage(deleteRequest);
            log.debug("SQS 메시지 삭제 완료: messageId={}", message.messageId());

        } catch (Exception e) {
            log.error("SQS 메시지 삭제 실패: messageId={}", message.messageId(), e);
        }
    }
}