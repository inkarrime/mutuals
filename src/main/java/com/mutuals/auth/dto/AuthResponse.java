package com.mutuals.auth.dto;

import com.mutuals.user.dto.MeResponse;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresIn,
        MeResponse user
) {
}
