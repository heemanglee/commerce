package com.practice.commerce.domain.product.event;

import com.practice.commerce.infrastructure.message.MessageQueueService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class ProductMediaEventListener {

    private final MessageQueueService messageQueueService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleProductMediaDelete(ProductMediaDeletedEvent event) {
        messageQueueService.sendS3DeletionMessages(event.productMedias());
    }
}
