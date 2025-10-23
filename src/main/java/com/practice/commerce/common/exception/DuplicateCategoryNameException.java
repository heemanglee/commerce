package com.practice.commerce.common.exception;

public class DuplicateCategoryNameException extends RuntimeException {

    public DuplicateCategoryNameException() {
    }

    public DuplicateCategoryNameException(String message) {
        super(message);
    }
}
