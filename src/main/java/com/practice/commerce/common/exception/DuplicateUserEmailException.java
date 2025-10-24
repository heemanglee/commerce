package com.practice.commerce.common.exception;

public class DuplicateUserEmailException extends RuntimeException {

    public DuplicateUserEmailException() {
    }

    public DuplicateUserEmailException(String message) {
        super(message);
    }
}
