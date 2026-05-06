package com.endava.personal.common.exception;

import com.endava.personal.common.error.ApiError;
import com.endava.personal.common.error.ApiErrorDetail;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex ) {
        List<ApiErrorDetail> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(this::toApiErrorDetail)
                .toList();

        ApiError body = ApiError.of(
                "VALIDATION_ERROR",
                "Request validation failed",
                errors
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> handleConstraintValidation(ConstraintViolationException ex ) {

        ApiError body = ApiError.of(
                "VALIDATION_ERROR",
                ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiError> handleBusinessException(BusinessException ex ) {
        log.error("Business exception: {} - {}", ex.getReason(), ex.getMessage());

        return ResponseEntity.status(ex.getStatusCode())
                .body(ApiError.of(ex.getReason(), ex.getMessage()));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        log.error("Data integrity violation", ex);

        ApiError body = ApiError.of(
                "DATA_INTEGRITY_VIOLATION",
                "Database constraint violation."
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnexpected(Exception ex) {
        log.error("Unexpected exception", ex);

        ApiError body = ApiError.of(
                "INTERNAL_SERVER_ERROR",
                "An unexpected error occurred."
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

    private ApiErrorDetail toApiErrorDetail(FieldError error) {
        return new ApiErrorDetail(error.getField(), error.getDefaultMessage());
    }
}
