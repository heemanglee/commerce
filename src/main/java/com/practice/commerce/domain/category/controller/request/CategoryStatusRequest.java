package com.practice.commerce.domain.category.controller.request;

import com.practice.commerce.domain.category.entity.CategoryStatus;

public enum CategoryStatusRequest {

    ACTIVE, INACTIVE;

    public CategoryStatus toEntityStatus() {
        return switch (this) {
            case ACTIVE -> CategoryStatus.ACTIVE;
            case INACTIVE -> CategoryStatus.INACTIVE;
        };
    }
}
