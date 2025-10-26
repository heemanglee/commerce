package com.practice.commerce.common.exception;

public class DuplicateUserNameException extends RuntimeException {
    public DuplicateUserNameException() {
    }

    public DuplicateUserNameException(String message) {
        super(message);
    }
}
