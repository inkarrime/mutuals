package com.mutuals.auth.service;

import com.mutuals.auth.dto.AuthResponse;
import com.mutuals.auth.dto.LoginRequest;
import com.mutuals.auth.dto.RegisterRequest;
import com.mutuals.auth.entity.RefreshToken;
import com.mutuals.common.exception.AccountSuspendedException;
import com.mutuals.common.exception.DuplicateResourceException;
import com.mutuals.common.exception.InvalidCredentialsException;
import com.mutuals.common.exception.InvalidTokenException;
import com.mutuals.event.UserRegisteredEvent;
import com.mutuals.security.JwtService;
import com.mutuals.security.UserPrincipal;
import com.mutuals.user.entity.ProfileCustomization;
import com.mutuals.user.entity.User;
import com.mutuals.user.entity.UserPreferences;
import com.mutuals.user.mapper.UserMapper;
import com.mutuals.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final String TOKEN_TYPE = "Bearer";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final UserMapper userMapper;
    private final ApplicationEventPublisher eventPublisher;
    private final Clock clock;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new DuplicateResourceException("Email is already registered");
        }
        if (userRepository.existsByUsername(request.username())) {
            throw new DuplicateResourceException("Username is already taken");
        }
        User user = new User();
        user.setEmail(email);
        user.setUsername(request.username());
        user.setDisplayName(request.displayName().trim());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setLastActiveAt(clock.instant());
        user.attachPreferences(new UserPreferences());
        user.attachCustomization(new ProfileCustomization());
        User saved = userRepository.save(user);
        eventPublisher.publishEvent(new UserRegisteredEvent(saved.getId()));
        return buildResponse(saved);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email().trim().toLowerCase(Locale.ROOT), request.password()));
        } catch (DisabledException ex) {
            throw new AccountSuspendedException();
        } catch (BadCredentialsException ex) {
            throw new InvalidCredentialsException();
        }
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        User user = userRepository.findById(principal.getId()).orElseThrow(InvalidCredentialsException::new);
        user.setLastActiveAt(clock.instant());
        return buildResponse(user);
    }

    @Transactional(noRollbackFor = InvalidTokenException.class)
    public AuthResponse refresh(String rawRefreshToken) {
        RefreshToken consumed = refreshTokenService.consume(rawRefreshToken);
        User user = consumed.getUser();
        if (!user.isActive()) {
            throw new AccountSuspendedException();
        }
        return buildResponse(user);
    }

    @Transactional
    public void logout(String rawRefreshToken) {
        refreshTokenService.revoke(rawRefreshToken);
    }

    private AuthResponse buildResponse(User user) {
        String accessToken = jwtService.generateAccessToken(UserPrincipal.from(user));
        String refreshToken = refreshTokenService.issue(user);
        return new AuthResponse(accessToken, refreshToken, TOKEN_TYPE, jwtService.getAccessTokenTtlSeconds(),
                userMapper.toMe(user));
    }
}
