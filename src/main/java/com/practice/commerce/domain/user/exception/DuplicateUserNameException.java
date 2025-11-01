package com.practice.commerce.domain.user.exception;

public class DuplicateUserNameException extends RuntimeException {
    public DuplicateUserNameException() {
    }

    public DuplicateUserNameException(String message) {
        super(message);
    }
}
