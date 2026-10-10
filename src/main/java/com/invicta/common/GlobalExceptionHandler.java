package com.invicta.common;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler({IllegalArgumentException.class, ConstraintViolationException.class})
    ResponseEntity<ApiError> badRequest(Exception exception) {
        return error(HttpStatus.BAD_REQUEST, "invalid_request", exception.getMessage(), Map.of());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiError> validation(MethodArgumentNotValidException exception) {
        Map<String, String> details = new LinkedHashMap<>();
        exception.getBindingResult().getFieldErrors()
                .forEach(error -> details.putIfAbsent(error.getField(), error.getDefaultMessage()));
        return error(HttpStatus.BAD_REQUEST, "validation_failed", "Request validation failed", details);
    }

    @ExceptionHandler(ForbiddenException.class)
    ResponseEntity<ApiError> forbidden(ForbiddenException exception) {
        return error(HttpStatus.FORBIDDEN, "forbidden", exception.getMessage(), Map.of());
    }

    @ExceptionHandler(TooManyCodeRequestsException.class)
    ResponseEntity<ApiError> tooManyCodeRequests(TooManyCodeRequestsException exception) {
        return error(HttpStatus.TOO_MANY_REQUESTS, "code_request_too_frequent", exception.getMessage(), Map.of());
    }

    @ExceptionHandler(VerificationCodeInvalidException.class)
    ResponseEntity<ApiError> invalidCode(VerificationCodeInvalidException exception) {
        return error(HttpStatus.BAD_REQUEST, "verification_code_expired", exception.getMessage(), Map.of());
    }

    @ExceptionHandler(AccountUnavailableException.class)
    ResponseEntity<ApiError> accountUnavailable(AccountUnavailableException exception) {
        return error(HttpStatus.NOT_FOUND, "account_unavailable", exception.getMessage(), Map.of());
    }

    @ExceptionHandler(EmailAlreadyRegisteredException.class)
    ResponseEntity<ApiError> emailAlreadyRegistered(EmailAlreadyRegisteredException exception) {
        return error(HttpStatus.CONFLICT, "email_already_registered", exception.getMessage(), Map.of());
    }

    private ResponseEntity<ApiError> error(HttpStatus status, String code, String message, Map<String, String> details) {
        return ResponseEntity.status(status).body(new ApiError(code, message, Instant.now(), details));
    }
}
