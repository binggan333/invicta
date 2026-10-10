package com.invicta.common;

import jakarta.validation.ConstraintViolationException;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private final MessageSource messages;

    public GlobalExceptionHandler(MessageSource messages) {
        this.messages = messages;
    }

    @ExceptionHandler({IllegalArgumentException.class, ConstraintViolationException.class})
    ResponseEntity<ApiError> badRequest(Exception exception) {
        return error(HttpStatus.BAD_REQUEST, "invalid_request", message("error.invalid-request"), Map.of());
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MissingServletRequestParameterException.class,
            MethodArgumentTypeMismatchException.class, HandlerMethodValidationException.class})
    ResponseEntity<ApiError> malformedRequest(Exception exception) {
        return error(HttpStatus.BAD_REQUEST, "invalid_request", message("error.invalid-request"), Map.of());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiError> validation(MethodArgumentNotValidException exception) {
        Map<String, String> details = new LinkedHashMap<>();
        exception.getBindingResult().getFieldErrors()
                .forEach(error -> details.putIfAbsent(error.getField(), error.getDefaultMessage()));
        return error(HttpStatus.BAD_REQUEST, "validation_failed", message("error.validation-failed"), details);
    }

    @ExceptionHandler(ForbiddenException.class)
    ResponseEntity<ApiError> forbidden(ForbiddenException exception) {
        return error(HttpStatus.FORBIDDEN, "forbidden", message("error.forbidden"), Map.of());
    }

    @ExceptionHandler(TooManyCodeRequestsException.class)
    ResponseEntity<ApiError> tooManyCodeRequests(TooManyCodeRequestsException exception) {
        return error(HttpStatus.TOO_MANY_REQUESTS, "code_request_too_frequent",
                message("error.code-request-too-frequent"), Map.of());
    }

    @ExceptionHandler(VerificationCodeInvalidException.class)
    ResponseEntity<ApiError> invalidCode(VerificationCodeInvalidException exception) {
        return error(HttpStatus.BAD_REQUEST, "verification_code_expired",
                message("error.verification-code-invalid"), Map.of());
    }

    @ExceptionHandler(AccountUnavailableException.class)
    ResponseEntity<ApiError> accountUnavailable(AccountUnavailableException exception) {
        return error(HttpStatus.NOT_FOUND, "account_unavailable", message("error.account-unavailable"), Map.of());
    }

    @ExceptionHandler(EmailAlreadyRegisteredException.class)
    ResponseEntity<ApiError> emailAlreadyRegistered(EmailAlreadyRegisteredException exception) {
        return error(HttpStatus.CONFLICT, "email_already_registered",
                message("error.email-already-registered"), Map.of());
    }

    @ExceptionHandler(EmailDeliveryException.class)
    ResponseEntity<ApiError> emailDelivery(EmailDeliveryException exception) {
        return error(HttpStatus.SERVICE_UNAVAILABLE, "verification_email_unavailable",
                message("error.verification-email-unavailable"), Map.of());
    }

    @ExceptionHandler(ResponseStatusException.class)
    ResponseEntity<ApiError> responseStatus(ResponseStatusException exception) {
        String reason = exception.getReason();
        String localized = reason != null && reason.startsWith("error.")
                ? message(reason)
                : message("error.request-failed");
        return error(exception.getStatusCode(), "request_failed", localized, Map.of());
    }

    private String message(String key) {
        return messages.getMessage(key, null, key, LocaleContextHolder.getLocale());
    }

    private ResponseEntity<ApiError> error(HttpStatusCode status, String code, String message,
                                           Map<String, String> details) {
        return ResponseEntity.status(status).body(new ApiError(code, message, Instant.now(), details));
    }
}
