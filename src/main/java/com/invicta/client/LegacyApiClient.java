package com.invicta.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.invicta.config.InvictaProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;

/**
 * Invicta 写操作调用旧平台的唯一入口。
 * 该客户端只能由明确的业务服务使用，禁止暴露为通用 HTTP 代理。
 */
@Component
public class LegacyApiClient {
    private static final Logger log = LoggerFactory.getLogger(LegacyApiClient.class);
    private final RestClient client;
    private final ObjectMapper objectMapper;
    private final InvictaProperties.LegacyApi properties;

    public LegacyApiClient(RestClient.Builder builder, ObjectMapper objectMapper, InvictaProperties properties) {
        this.properties = properties.legacyApi();
        this.objectMapper = objectMapper;
        RestClient.Builder configured = builder.baseUrl(this.properties.baseUrl());
        if (this.properties.token() != null && !this.properties.token().isBlank()) {
            // UTC Pay 的 Shiro 过滤器直接校验原始令牌，不接受 Bearer 前缀。
            configured.defaultHeader("Authorization", this.properties.token());
        }
        this.client = configured.build();
    }

    public <T, R> R post(String relativePath, T body, Class<R> responseType) {
        requireRelative(relativePath);
        log.info("调用UTC Pay method=POST path={} signed=false", relativePath);
        return client.post().uri(relativePath).contentType(MediaType.APPLICATION_JSON)
                .body(body).retrieve().body(responseType);
    }

    public <T, R> R postSigned(String relativePath, T body, Class<R> responseType) {
        requireRelative(relativePath);
        requireApiCredentials();
        String timestamp = Long.toString(Instant.now().getEpochSecond());
        String json = json(body);
        String signature = sign(timestamp + "POST" + relativePath + json);
        log.info("调用UTC Pay method=POST path={} signed=true", relativePath);
        return client.post().uri(relativePath)
                .header("X-PAY-KEY", properties.apiKey())
                .header("X-PAY-SIGN", signature)
                .header("X-PAY-TIMESTAMP", timestamp)
                .contentType(MediaType.APPLICATION_JSON)
                .body(json).retrieve().body(responseType);
    }

    public <T, R> R put(String relativePath, T body, Class<R> responseType) {
        requireRelative(relativePath);
        log.info("调用UTC Pay method=PUT path={} signed=false", relativePath);
        return client.put().uri(relativePath).contentType(MediaType.APPLICATION_JSON)
                .body(body).retrieve().body(responseType);
    }

    private void requireRelative(String path) {
        if (path == null || !path.startsWith("/") || path.startsWith("//") || path.contains("://")) {
            throw new IllegalArgumentException("UTC Pay API 路径必须为相对路径");
        }
    }

    private void requireApiCredentials() {
        if (properties.apiKey() == null || properties.apiKey().isBlank()
                || properties.secretKey() == null || properties.secretKey().isBlank()) {
            throw new IllegalStateException("LEGACY_API_KEY 和 LEGACY_API_SECRET 不能为空");
        }
    }

    private String json(Object body) {
        try {
            return objectMapper.writeValueAsString(body);
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException("请求体序列化失败", exception);
        }
    }

    private String sign(String content) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(properties.secretKey().getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return Base64.getEncoder().encodeToString(mac.doFinal(content.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception exception) {
            throw new IllegalStateException("API 请求签名失败", exception);
        }
    }
}
