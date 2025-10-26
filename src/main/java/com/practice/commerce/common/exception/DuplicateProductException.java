package com.practice.commerce.common.exception;

public class DuplicateProductException extends RuntimeException {
    public DuplicateProductException() {
    }

    public DuplicateProductException(String message) {
        super(message);
    }
}
