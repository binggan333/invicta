package com.invicta.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Locale;

public final class EmailAddressUtils {
    private EmailAddressUtils() {}

    public static String normalize(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    public static String hash(String email) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(normalize(email).getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }

    public static String mask(String email) {
        String normalized = normalize(email);
        int at = normalized.indexOf('@');
        return at <= 1 ? "***" : normalized.charAt(0) + "***" + normalized.substring(at);
    }
}
