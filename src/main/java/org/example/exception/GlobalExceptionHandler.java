package org.example.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // 400: ошибка валидации @Valid @RequestBody
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex,
                                                     HttpServletRequest req) {

        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(GlobalExceptionHandler::formatFieldError)
                .collect(Collectors.joining("; "));

        ApiError body = new ApiError(
                Instant.now().toString(),
                req.getRequestURI(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                message
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    private static String formatFieldError(FieldError fe) {
        String field = fe.getField();
        String defaultMsg = fe.getDefaultMessage();
        return field + ": " + (defaultMsg == null ? "invalid" : defaultMsg);
    }

    // 400: кривой JSON или пустое тело
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleBadJson(HttpMessageNotReadableException ex,
                                                  HttpServletRequest req) {
        ApiError body = new ApiError(
                Instant.now().toString(),
                req.getRequestURI(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "Malformed JSON or missing request body"
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    // 404: наш кастомный NotFoundException
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(NotFoundException ex,
                                                   HttpServletRequest req) {
        ApiError body = new ApiError(
                Instant.now().toString(),
                req.getRequestURI(),
                HttpStatus.NOT_FOUND.getReasonPhrase(),
                ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    // 404: на всякий случай для NoSuchElementException
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ApiError> handleNoSuchElement(NoSuchElementException ex,
                                                        HttpServletRequest req) {
        ApiError body = new ApiError(
                Instant.now().toString(),
                req.getRequestURI(),
                HttpStatus.NOT_FOUND.getReasonPhrase(),
                ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    // 409: конфликт (дубликаты и т.п.)
    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiError> handleConflict(ConflictException ex,
                                                   HttpServletRequest req) {
        ApiError body = new ApiError(
                Instant.now().toString(),
                req.getRequestURI(),
                HttpStatus.CONFLICT.getReasonPhrase(),
                ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    // 403: отказ в доступе (в т.ч. @PreAuthorize hasRole(...) и т.п.)
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> handleAccessDenied(AccessDeniedException ex,
                                                       HttpServletRequest req) {
        ApiError body = new ApiError(
                Instant.now().toString(),
                req.getRequestURI(),
                HttpStatus.FORBIDDEN.getReasonPhrase(),
                ex.getMessage() != null ? ex.getMessage() : "Access denied"
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
    }

    // 500: всё, что не перехватили более специфичные хендлеры
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleAny(Exception ex,
                                              HttpServletRequest req) {
        log.error("Unhandled error in {} {}", req.getMethod(), req.getRequestURI(), ex);

        ApiError body = new ApiError(
                Instant.now().toString(),
                req.getRequestURI(),
                HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                "Unexpected error"
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}