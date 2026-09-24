package com.mutuals.security;

import com.mutuals.common.exception.AccountSuspendedException;
import com.mutuals.common.exception.UnauthorizedException;
import com.mutuals.user.entity.User;
import com.mutuals.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CurrentUserService {

    private final UserRepository userRepository;

    public Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal principal)) {
            throw new UnauthorizedException("Authentication required");
        }
        return principal.getId();
    }

    public User getCurrentUser() {
        User user = userRepository.findById(getCurrentUserId())
                .orElseThrow(() -> new UnauthorizedException("Authenticated user no longer exists"));
        if (!user.isActive()) {
            throw new AccountSuspendedException();
        }
        return user;
    }
}
