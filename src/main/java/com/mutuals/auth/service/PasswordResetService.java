package com.mutuals.auth.service;

import com.mutuals.auth.entity.PasswordResetToken;
import com.mutuals.auth.repository.PasswordResetTokenRepository;
import com.mutuals.common.exception.InvalidTokenException;
import com.mutuals.common.util.TokenHasher;
import com.mutuals.event.PasswordResetRequestedEvent;
import com.mutuals.user.entity.User;
import com.mutuals.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private static final Duration TOKEN_TTL = Duration.ofMinutes(30);

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;
    private final ApplicationEventPublisher eventPublisher;
    private final Clock clock;

    @Transactional
    public void requestReset(String email) {
        userRepository.findByEmailIgnoreCase(email.trim())
                .filter(User::isActive)
                .ifPresent(this::createToken);
    }

    @Transactional
    public void confirmReset(String rawToken, String newPassword) {
        Instant now = clock.instant();
        PasswordResetToken token = passwordResetTokenRepository.findByTokenHash(TokenHasher.sha256(rawToken))
                .filter(candidate -> candidate.isUsable(now))
                .orElseThrow(() -> new InvalidTokenException("Password reset token is invalid or expired"));
        token.setUsedAt(now);
        User user = token.getUser();
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        refreshTokenService.revokeAll(user.getId());
    }

    private void createToken(User user) {
        String rawToken = TokenHasher.newRawToken();
        PasswordResetToken token = new PasswordResetToken();
        token.setUser(user);
        token.setTokenHash(TokenHasher.sha256(rawToken));
        token.setExpiresAt(clock.instant().plus(TOKEN_TTL));
        passwordResetTokenRepository.save(token);
        eventPublisher.publishEvent(new PasswordResetRequestedEvent(user.getId(), rawToken));
    }
}
