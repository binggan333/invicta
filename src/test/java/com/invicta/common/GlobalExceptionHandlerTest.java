package com.invicta.common;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.http.ResponseEntity;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {
    private final GlobalExceptionHandler handler = new GlobalExceptionHandler(messageSource());

    @AfterEach
    void resetLocale() {
        LocaleContextHolder.resetLocaleContext();
    }

    @Test
    void returnsChineseVerificationCodeError() {
        LocaleContextHolder.setLocale(Locale.SIMPLIFIED_CHINESE);

        ResponseEntity<ApiError> response = handler.invalidCode(new VerificationCodeInvalidException());

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).isEqualTo("验证码已失效");
    }

    @Test
    void returnsEnglishRateLimitError() {
        LocaleContextHolder.setLocale(Locale.US);

        ResponseEntity<ApiError> response = handler.tooManyCodeRequests(new TooManyCodeRequestsException());

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).isEqualTo("Too many requests. Please try again later.");
    }

    private static ResourceBundleMessageSource messageSource() {
        ResourceBundleMessageSource source = new ResourceBundleMessageSource();
        source.setBasename("i18n/messages");
        source.setDefaultEncoding("UTF-8");
        source.setFallbackToSystemLocale(false);
        return source;
    }
}
