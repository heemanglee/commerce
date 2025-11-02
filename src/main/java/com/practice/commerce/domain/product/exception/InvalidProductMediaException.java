package com.practice.commerce.domain.product.exception;

public class InvalidProductMediaException extends RuntimeException {
    private final String errorCode;

    public InvalidProductMediaException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

}
