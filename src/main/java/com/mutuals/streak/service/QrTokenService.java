package com.mutuals.streak.service;

import com.mutuals.config.AppProperties;
import com.mutuals.streak.dto.QrCodeResponse;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.time.Clock;
import java.time.Instant;
import java.util.Arrays;
import java.util.Base64;
import java.util.Optional;

@Service
public class QrTokenService {

    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final int SIGNATURE_BYTES = 16;

    private final byte[] secret;
    private final long windowSeconds;
    private final Clock clock;

    public QrTokenService(AppProperties properties, Clock clock) {
        this.secret = properties.qr().secret().getBytes(StandardCharsets.UTF_8);
        this.windowSeconds = properties.qr().tokenSeconds();
        this.clock = clock;
    }

    public QrCodeResponse generate(Long userId) {
        long window = currentWindow();
        String payload = userId + "." + window;
        String token = payload + "." + sign(payload);
        return new QrCodeResponse(token, Instant.ofEpochSecond((window + 1) * windowSeconds));
    }

    public Optional<Long> verify(String token) {
        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            return Optional.empty();
        }
        try {
            long userId = Long.parseLong(parts[0]);
            long window = Long.parseLong(parts[1]);
            long current = currentWindow();
            boolean fresh = window == current || window == current - 1;
            String expected = sign(parts[0] + "." + parts[1]);
            boolean validSignature = MessageDigest.isEqual(
                    expected.getBytes(StandardCharsets.UTF_8), parts[2].getBytes(StandardCharsets.UTF_8));
            return fresh && validSignature ? Optional.of(userId) : Optional.empty();
        } catch (NumberFormatException ex) {
            return Optional.empty();
        }
    }

    private long currentWindow() {
        return clock.instant().getEpochSecond() / windowSeconds;
    }

    private String sign(String payload) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(secret, HMAC_ALGORITHM));
            byte[] digest = Arrays.copyOf(mac.doFinal(payload.getBytes(StandardCharsets.UTF_8)), SIGNATURE_BYTES);
            return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
        } catch (GeneralSecurityException ex) {
            throw new IllegalStateException("Unable to sign QR token", ex);
        }
    }
}
