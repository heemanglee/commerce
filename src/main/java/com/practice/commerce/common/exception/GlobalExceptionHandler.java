package com.practice.commerce.common.exception;

import com.practice.commerce.domain.category.exception.DuplicateCategoryNameException;
import com.practice.commerce.domain.category.exception.NotFoundCategoryException;
import com.practice.commerce.domain.product.exception.DuplicateProductException;
import com.practice.commerce.domain.product.exception.InvalidProductMediaException;
import com.practice.commerce.domain.product.exception.NotFoundProductException;
import com.practice.commerce.domain.user.exception.DuplicateUserEmailException;
import com.practice.commerce.domain.user.exception.DuplicateUserNameException;
import com.practice.commerce.domain.user.exception.NotFoundUserException;
import com.practice.commerce.infrastructure.s3.exception.FileUploadException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
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

    @ExceptionHandler(NotFoundUserException.class)
    public ResponseEntity<ErrorResponse> handleNotFoundUserException(NotFoundUserException ex) {
        ErrorResponse errorResponse = new ErrorResponse(
            "USER_NOT_FOUND",
            ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler(DuplicateProductException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateProductException(DuplicateProductException ex) {
        ErrorResponse errorResponse = new ErrorResponse(
            "PRODUCT_NOT_FOUND",
            ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    @ExceptionHandler(DuplicateUserNameException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateUserNameException(DuplicateUserNameException ex) {
        ErrorResponse errorResponse = new ErrorResponse(
            "USER_NAME_ALREADY_EXISTS",
            ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentialsException(BadCredentialsException ex) {
        ErrorResponse errorResponse = new ErrorResponse(
            "BAD_CREDENTIALS",
            ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    @ExceptionHandler(FileUploadException.class)
    public ResponseEntity<ErrorResponse> handleFileUploadException(FileUploadException ex) {
        ErrorResponse errorResponse = new ErrorResponse(
            "FILE_UPLOAD_ERROR",
            ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(InvalidProductMediaException.class)
    public ResponseEntity<ErrorResponse> handleInvalidProductMediaException(InvalidProductMediaException ex) {
        ErrorResponse errorResponse = new ErrorResponse(
            "INVALID_PRODUCT_MEDIA_ERROR",
            ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(NotFoundProductException.class)
    public ResponseEntity<ErrorResponse> handleNotFoundProductException(NotFoundProductException ex) {
        ErrorResponse errorResponse = new ErrorResponse(
            "PRODUCT_NOT_FOUND",
            ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
        ErrorResponse errorResponse = new ErrorResponse(
            "INVALID_ARGUMENT",
            ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        String errorMessage = ex.getBindingResult().getFieldErrors().stream()
            .map(error -> String.format("%s: %s", error.getField(), error.getDefaultMessage()))
            .reduce((a, b) -> a + ", " + b)
            .orElse("Validation failed");

        ErrorResponse errorResponse = new ErrorResponse(
            "INVALID_ARGUMENT",
            errorMessage
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
}
