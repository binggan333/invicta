package com.invicta.common;

import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
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
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private final MessageSource messages;

    public GlobalExceptionHandler(MessageSource messages) {
        this.messages = messages;
    }

    @ExceptionHandler({IllegalArgumentException.class, ConstraintViolationException.class})
    ResponseEntity<ApiError> badRequest(Exception exception) {
        return error(ApiErrorCode.INVALID_REQUEST, Map.of());
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MissingServletRequestParameterException.class,
            MethodArgumentTypeMismatchException.class, HandlerMethodValidationException.class})
    ResponseEntity<ApiError> malformedRequest(Exception exception) {
        return error(ApiErrorCode.INVALID_REQUEST, Map.of());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiError> validation(MethodArgumentNotValidException exception) {
        Map<String, String> details = new LinkedHashMap<>();
        exception.getBindingResult().getFieldErrors()
                .forEach(error -> details.putIfAbsent(error.getField(), error.getDefaultMessage()));
        return error(ApiErrorCode.VALIDATION_FAILED, details);
    }

    @ExceptionHandler(ForbiddenException.class)
    ResponseEntity<ApiError> forbidden(ForbiddenException exception) {
        return error(ApiErrorCode.FORBIDDEN, Map.of());
    }

    @ExceptionHandler(TooManyCodeRequestsException.class)
    ResponseEntity<ApiError> tooManyCodeRequests(TooManyCodeRequestsException exception) {
        return error(ApiErrorCode.CODE_REQUEST_TOO_FREQUENT, Map.of());
    }

    @ExceptionHandler(VerificationCodeInvalidException.class)
    ResponseEntity<ApiError> invalidCode(VerificationCodeInvalidException exception) {
        return error(ApiErrorCode.VERIFICATION_CODE_INVALID, Map.of());
    }

    @ExceptionHandler(AccountUnavailableException.class)
    ResponseEntity<ApiError> accountUnavailable(AccountUnavailableException exception) {
        return error(ApiErrorCode.ACCOUNT_UNAVAILABLE, Map.of());
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    ResponseEntity<ApiError> invalidCredentials(InvalidCredentialsException exception) {
        return error(ApiErrorCode.INVALID_CREDENTIALS, Map.of());
    }

    @ExceptionHandler(EmailAlreadyRegisteredException.class)
    ResponseEntity<ApiError> emailAlreadyRegistered(EmailAlreadyRegisteredException exception) {
        return error(ApiErrorCode.EMAIL_ALREADY_REGISTERED, Map.of());
    }

    @ExceptionHandler(ExistingAccountRequiredException.class)
    ResponseEntity<ApiError> existingAccountRequired(ExistingAccountRequiredException exception) {
        return error(ApiErrorCode.EXISTING_ACCOUNT_REQUIRED, Map.of());
    }

    @ExceptionHandler(ExistingAccountUnavailableException.class)
    ResponseEntity<ApiError> existingAccountUnavailable(ExistingAccountUnavailableException exception) {
        return error(ApiErrorCode.EXISTING_ACCOUNT_UNAVAILABLE, Map.of());
    }

    @ExceptionHandler(EmailDeliveryException.class)
    ResponseEntity<ApiError> emailDelivery(EmailDeliveryException exception) {
        return error(ApiErrorCode.VERIFICATION_EMAIL_UNAVAILABLE, Map.of());
    }

    @ExceptionHandler(ResponseStatusException.class)
    ResponseEntity<ApiError> responseStatus(ResponseStatusException exception) {
        ApiErrorCode code = ApiErrorCode.forReason(exception.getReason())
                .orElse(ApiErrorCode.REQUEST_FAILED);
        return error(exception.getStatusCode(), code, Map.of());
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiError> unexpected(Exception exception) {
        log.error("未处理的服务异常 type={}", exception.getClass().getSimpleName(), exception);
        return error(ApiErrorCode.INTERNAL_SERVER_ERROR, Map.of());
    }

    private String message(String key) {
        return messages.getMessage(key, null, key, LocaleContextHolder.getLocale());
    }

    private ResponseEntity<ApiError> error(ApiErrorCode code, Map<String, String> details) {
        return error(code.status(), code, details);
    }

    private ResponseEntity<ApiError> error(HttpStatusCode status, ApiErrorCode code,
                                           Map<String, String> details) {
        if (status.is4xxClientError()) {
            log.warn("请求被拒绝 status={} code={}", status.value(), code.code());
        }
        return ResponseEntity.status(status).body(
                new ApiError(code.code(), message(code.messageKey()), Instant.now(), MDC.get("requestId"), details));
    }
}
