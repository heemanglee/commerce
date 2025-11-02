package com.practice.commerce.domain.product.exception;

public class NotFoundProductException extends RuntimeException {
    public NotFoundProductException() {
    }

    public NotFoundProductException(String message) {
        super(message);
    }
}
