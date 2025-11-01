package com.practice.commerce.domain.user.exception;

public class DuplicateUserEmailException extends RuntimeException {

    public DuplicateUserEmailException() {
    }

    public DuplicateUserEmailException(String message) {
        super(message);
    }
}
