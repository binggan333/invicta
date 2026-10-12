package com.invicta.client;

import com.invicta.config.InvictaProperties;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * The only supported path for Invicta mutations. Keep this client behind purpose-built services;
 * never expose it as a generic HTTP proxy.
 */
@Component
public class LegacyApiClient {
    private final RestClient client;

    public LegacyApiClient(RestClient.Builder builder, InvictaProperties properties) {
        RestClient.Builder configured = builder.baseUrl(properties.legacyApi().baseUrl());
        if (properties.legacyApi().token() != null && !properties.legacyApi().token().isBlank()) {
            // UTC Pay's Shiro filter expects the stored access token itself, without a Bearer prefix.
            configured.defaultHeader("Authorization", properties.legacyApi().token());
        }
        this.client = configured.build();
    }

    public <T, R> R post(String relativePath, T body, Class<R> responseType) {
        requireRelative(relativePath);
        return client.post().uri(relativePath).contentType(MediaType.APPLICATION_JSON)
                .body(body).retrieve().body(responseType);
    }

    public <T, R> R put(String relativePath, T body, Class<R> responseType) {
        requireRelative(relativePath);
        return client.put().uri(relativePath).contentType(MediaType.APPLICATION_JSON)
                .body(body).retrieve().body(responseType);
    }

    private void requireRelative(String path) {
        if (path == null || !path.startsWith("/") || path.startsWith("//") || path.contains("://")) {
            throw new IllegalArgumentException("Legacy API path must be relative");
        }
    }
}
