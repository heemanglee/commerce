package com.practice.commerce.common.exception;

public class NotFoundCategoryException extends RuntimeException {
    public NotFoundCategoryException() {
    }

    public NotFoundCategoryException(String message) {
        super(message);
    }
}
