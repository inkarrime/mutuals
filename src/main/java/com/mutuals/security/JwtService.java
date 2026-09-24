package com.mutuals.security;

import com.mutuals.config.AppProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class JwtService {

    private static final String CLAIM_USER_ID = "userId";
    private static final String CLAIM_ROLES = "roles";

    private final SecretKey signingKey;
    private final Duration accessTokenTtl;
    private final Clock clock;

    public JwtService(AppProperties properties, Clock clock) {
        byte[] secret = properties.jwt().secret().getBytes(StandardCharsets.UTF_8);
        if (secret.length < 32) {
            throw new IllegalStateException("JWT_SECRET must be at least 32 bytes long");
        }
        this.signingKey = Keys.hmacShaKeyFor(secret);
        this.accessTokenTtl = Duration.ofMinutes(properties.jwt().accessTokenMinutes());
        this.clock = clock;
    }

    public String generateAccessToken(UserPrincipal principal) {
        Instant now = clock.instant();
        List<String> roles = principal.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();
        return Jwts.builder()
                .subject(principal.getEmail())
                .claim(CLAIM_USER_ID, principal.getId())
                .claim(CLAIM_ROLES, roles)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(accessTokenTtl)))
                .signWith(signingKey)
                .compact();
    }

    public long getAccessTokenTtlSeconds() {
        return accessTokenTtl.toSeconds();
    }

    public Optional<Claims> parseValidClaims(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(signingKey)
                    .clock(() -> Date.from(clock.instant()))
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return Optional.of(claims);
        } catch (JwtException | IllegalArgumentException ex) {
            return Optional.empty();
        }
    }

    public Long extractUserId(Claims claims) {
        return claims.get(CLAIM_USER_ID, Long.class);
    }
}
