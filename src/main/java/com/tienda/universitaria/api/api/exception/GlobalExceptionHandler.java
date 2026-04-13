package com.tienda.universitaria.api.api.exception;

import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(ResourceNotFoundException ex, WebRequest req) {
        var body = ApiError.of(HttpStatus.NOT_FOUND, ex.getMessage(), req.getDescription(false), List.of());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiError> handleBusiness(BusinessException ex, WebRequest req) {
        var body = ApiError.of(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage(), req.getDescription(false), List.of());
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(body);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiError> handleConflict(ConflictException ex, WebRequest req) {
        var body = ApiError.of(HttpStatus.CONFLICT, ex.getMessage(), req.getDescription(false), List.of());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ApiError> handleValidation(ValidationException ex, WebRequest req) {
        var body = ApiError.of(HttpStatus.BAD_REQUEST, ex.getMessage(), req.getDescription(false), List.of());
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, WebRequest req) {
        var violations = ex.getBindingResult().getFieldErrors()
                .stream().map(fe -> new ApiError.FieldViolation(fe.getField(), fe.getDefaultMessage())).toList();
        var body = ApiError.of(HttpStatus.BAD_REQUEST, "Validation failed", req.getDescription(false), violations);
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneral(Exception ex, WebRequest req) {
        var body = ApiError.of(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred", req.getDescription(false), List.of());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}
