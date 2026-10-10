package com.invicta.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;

import java.util.Locale;

@Configuration
public class LocaleConfig {
    @Bean
    LocaleResolver localeResolver() {
        return new LocaleResolver() {
            @Override
            public Locale resolveLocale(HttpServletRequest request) {
                String language = request.getParameter("lang");
                if (language != null && !language.isBlank()) {
                    return supportedLocale(language);
                }
                String acceptLanguage = request.getHeader("Accept-Language");
                if (acceptLanguage != null && !acceptLanguage.isBlank()) {
                    return supportedLocale(request.getLocale().toLanguageTag());
                }
                return Locale.US;
            }

            @Override
            public void setLocale(HttpServletRequest request, HttpServletResponse response, Locale locale) {
                // Invicta is stateless; callers select the language on every request.
            }
        };
    }

    private static Locale supportedLocale(String language) {
        String normalized = language.replace('_', '-').trim();
        return normalized.toLowerCase(Locale.ROOT).startsWith("zh")
                ? Locale.SIMPLIFIED_CHINESE
                : Locale.US;
    }
}
