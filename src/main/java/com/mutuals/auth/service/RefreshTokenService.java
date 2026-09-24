package com.mutuals.auth.service;

import com.mutuals.auth.entity.RefreshToken;
import com.mutuals.auth.repository.RefreshTokenRepository;
import com.mutuals.common.exception.InvalidTokenException;
import com.mutuals.common.util.TokenHasher;
import com.mutuals.config.AppProperties;
import com.mutuals.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final AppProperties properties;
    private final Clock clock;

    @Transactional
    public String issue(User user) {
        String rawToken = TokenHasher.newRawToken();
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setTokenHash(TokenHasher.sha256(rawToken));
        refreshToken.setExpiresAt(clock.instant().plus(Duration.ofDays(properties.jwt().refreshTokenDays())));
        refreshTokenRepository.save(refreshToken);
        return rawToken;
    }

    @Transactional(noRollbackFor = InvalidTokenException.class)
    public RefreshToken consume(String rawToken) {
        Instant now = clock.instant();
        RefreshToken refreshToken = refreshTokenRepository.findByTokenHash(TokenHasher.sha256(rawToken))
                .orElseThrow(() -> new InvalidTokenException("Refresh token is invalid"));
        if (refreshToken.getRevokedAt() != null) {
            refreshTokenRepository.revokeAllForUser(refreshToken.getUser().getId(), now);
            throw new InvalidTokenException("Refresh token was already used; all sessions were revoked");
        }
        if (!refreshToken.isUsable(now)) {
            throw new InvalidTokenException("Refresh token has expired");
        }
        refreshToken.setRevokedAt(now);
        return refreshToken;
    }

    @Transactional
    public void revoke(String rawToken) {
        refreshTokenRepository.findByTokenHash(TokenHasher.sha256(rawToken))
                .filter(token -> token.getRevokedAt() == null)
                .ifPresent(token -> token.setRevokedAt(clock.instant()));
    }

    @Transactional
    public void revokeAll(Long userId) {
        refreshTokenRepository.revokeAllForUser(userId, clock.instant());
    }
}
