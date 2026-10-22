package com.invicta.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.invicta.common.ApiError;
import com.invicta.common.ApiErrorCode;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.LocaleResolver;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Configuration
public class SecurityConfig {
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, ObjectMapper objectMapper,
                                            MessageSource messages, LocaleResolver localeResolver) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(errors -> errors
                        .authenticationEntryPoint((request, response, exception) -> {
                            response.setHeader(HttpHeaders.WWW_AUTHENTICATE, "Bearer");
                            writeSecurityError(request, response, objectMapper, messages, localeResolver,
                                    ApiErrorCode.AUTHENTICATION_REQUIRED);
                        })
                        .accessDeniedHandler((request, response, exception) -> {
                            ApiErrorCode code = SecurityContextHolder.getContext().getAuthentication()
                                    instanceof JwtAuthenticationToken token
                                    && !Boolean.TRUE.equals(token.getToken().getClaim("profileCompleted"))
                                    ? ApiErrorCode.PROFILE_INCOMPLETE : ApiErrorCode.FORBIDDEN;
                            writeSecurityError(request, response, objectMapper, messages, localeResolver, code);
                        }))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v1/auth/email/**",
                                "/api/v1/auth/existing/**",
                                "/api/v1/auth/password/login",
                                "/api/v1/auth/register",
                                "/actuator/health/**", "/v3/api-docs/**",
                                "/swagger-ui.html", "/swagger-ui/**").permitAll()
                        .requestMatchers("/api/v1/auth/me", "/api/v1/auth/profile",
                                "/api/v1/organization/me").authenticated()
                        .anyRequest().access((authentication, context) -> {
                            if (authentication.get() instanceof JwtAuthenticationToken token) {
                                return new AuthorizationDecision(Boolean.TRUE.equals(
                                        token.getToken().getClaim("profileCompleted")));
                            }
                            return new AuthorizationDecision(false);
                        }))
                .oauth2ResourceServer(resource -> resource.jwt(jwt -> {}))
                .build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource(
            @Value("${invicta.cors.allowed-origins:}") String allowedOrigins) {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isEmpty())
                .toList());
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept", "Accept-Language"));
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        return source;
    }

    private void writeSecurityError(HttpServletRequest request, HttpServletResponse response,
                                    ObjectMapper objectMapper, MessageSource messages, LocaleResolver localeResolver,
                                    ApiErrorCode code) throws IOException {
        response.setStatus(code.status().value());
        response.setContentType("application/json;charset=UTF-8");
        objectMapper.writeValue(response.getOutputStream(), new ApiError(code.code(),
                messages.getMessage(code.messageKey(), null, localeResolver.resolveLocale(request)),
                Instant.now(), response.getHeader(RequestLoggingFilter.REQUEST_ID_HEADER), Map.of()));
    }

    @Bean
    SecretKey jwtSecretKey(InvictaProperties properties) {
        byte[] bytes = properties.auth().jwtSecret().getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            throw new IllegalStateException("JWT_SECRET 至少需要 32 字节");
        }
        return new SecretKeySpec(bytes, "HmacSHA256");
    }

    @Bean
    JwtDecoder jwtDecoder(SecretKey secretKey) {
        return NimbusJwtDecoder.withSecretKey(secretKey).build();
    }

    @Bean
    JwtEncoder jwtEncoder(SecretKey secretKey) {
        return new NimbusJwtEncoder(new ImmutableSecret<>(secretKey));
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}
