package com.mutuals.streak.service;

import com.mutuals.config.AppProperties;
import com.mutuals.streak.dto.QrCodeResponse;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class QrTokenServiceTest {

    private static final Instant NOW = Instant.parse("2026-09-25T15:00:00Z");

    private QrTokenService serviceAt(Instant instant) {
        AppProperties properties = new AppProperties("America/Lima", null, null, null, null,
                new AppProperties.Qr("unit-test-secret-unit-test-secret-123", 30), null, null, null, null, null);
        return new QrTokenService(properties, Clock.fixed(instant, ZoneOffset.UTC));
    }

    @Test
    void acceptsFreshTokenForItsOwner() {
        QrCodeResponse code = serviceAt(NOW).generate(42L);
        assertThat(serviceAt(NOW.plusSeconds(10)).verify(code.token())).contains(42L);
    }

    @Test
    void rejectsExpiredToken() {
        QrCodeResponse code = serviceAt(NOW).generate(42L);
        assertThat(serviceAt(NOW.plusSeconds(120)).verify(code.token())).isEmpty();
    }

    @Test
    void rejectsTamperedToken() {
        String token = serviceAt(NOW).generate(42L).token();
        String tampered = "43" + token.substring(token.indexOf('.'));
        assertThat(serviceAt(NOW).verify(tampered)).isEmpty();
        assertThat(List.of("", "abc", "1.2")).allSatisfy(bad -> assertThat(serviceAt(NOW).verify(bad)).isEmpty());
    }
}
