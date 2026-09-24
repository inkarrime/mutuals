package com.mutuals.user.service;

import com.mutuals.common.exception.ResourceNotFoundException;
import com.mutuals.user.entity.User;
import com.mutuals.user.entity.UserStatus;
import com.mutuals.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserLookupService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public User getActiveUser(Long userId) {
        return userRepository.findById(userId)
                .filter(user -> user.getStatus() == UserStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
    }

    @Transactional(readOnly = true)
    public User getByUsername(String username) {
        return userRepository.findByUsername(username)
                .filter(user -> user.getStatus() == UserStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("User", username));
    }
}
