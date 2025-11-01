package com.practice.commerce.domain.category.exception;

public class DuplicateCategoryNameException extends RuntimeException {

    public DuplicateCategoryNameException() {
    }

    public DuplicateCategoryNameException(String message) {
        super(message);
    }
}
