package com.mutuals.streak.dto;

import java.time.Instant;

public record QrCodeResponse(String token, Instant expiresAt) {
}
