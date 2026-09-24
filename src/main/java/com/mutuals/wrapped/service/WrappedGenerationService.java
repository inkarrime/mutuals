package com.mutuals.wrapped.service;

import com.mutuals.user.entity.User;
import com.mutuals.user.entity.UserStatus;
import com.mutuals.user.repository.UserRepository;
import com.mutuals.wrapped.dto.WrappedPeriod;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class WrappedGenerationService {

    private final UserRepository userRepository;
    private final WrappedService wrappedService;

    @Async
    public void generateForAllUsers(WrappedPeriod period) {
        List<Long> userIds = userRepository.findByStatus(UserStatus.ACTIVE).stream().map(User::getId).toList();
        int generated = 0;
        for (Long userId : userIds) {
            try {
                wrappedService.generateForUser(userId, period);
                generated++;
            } catch (RuntimeException ex) {
                log.warn("Wrapped {} failed for user {}: {}", period.key(), userId, ex.getMessage());
            }
        }
        log.info("Wrapped {} generated for {} of {} users", period.key(), generated, userIds.size());
    }
}
