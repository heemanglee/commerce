package com.practice.commerce.domain.product.event;

import com.practice.commerce.infrastructure.message.S3DeletionMessage;
import java.util.List;

public record ProductMediaDeletedEvent(
        List<S3DeletionMessage> productMedias
) {

}
