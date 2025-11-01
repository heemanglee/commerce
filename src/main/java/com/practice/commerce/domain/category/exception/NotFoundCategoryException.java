package com.practice.commerce.domain.category.exception;

public class NotFoundCategoryException extends RuntimeException {
    public NotFoundCategoryException() {
    }

    public NotFoundCategoryException(String message) {
        super(message);
    }
}
