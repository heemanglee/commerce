package com.practice.commerce.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DuplicateCategoryNameException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateCategoryNameException(DuplicateCategoryNameException ex) {
        ErrorResponse errorResponse = new ErrorResponse(
                "DUPLICATE_CATEGORY_NAME",
                ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    @ExceptionHandler(NotFoundCategoryException.class)
    public ResponseEntity<ErrorResponse> handleNotFoundCategoryException(NotFoundCategoryException ex) {
        ErrorResponse errorResponse = new ErrorResponse(
                "CATEGORY_NOT_FOUND",
                ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler(DuplicateUserEmailException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateUserEmailException(DuplicateUserEmailException ex) {
        ErrorResponse errorResponse = new ErrorResponse(
                "DUPLICATE_USER_EMAIL",
                ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }
}
