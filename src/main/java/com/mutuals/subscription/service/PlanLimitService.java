package com.mutuals.subscription.service;

import com.mutuals.common.exception.PlanLimitExceededException;
import com.mutuals.config.AppProperties;
import com.mutuals.streak.entity.StreakStatus;
import com.mutuals.streak.repository.StreakRepository;
import com.mutuals.user.entity.Role;
import com.mutuals.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PlanLimitService {

    private final StreakRepository streakRepository;
    private final AppProperties properties;

    @Transactional(readOnly = true)
    public void assertCanStartStreak(User user) {
        if (user.hasRole(Role.PREMIUM)) {
            return;
        }
        int limit = properties.streak().maxActiveStreaksFree();
        if (streakRepository.countByUserAndStatus(user.getId(), StreakStatus.ACTIVE) >= limit) {
            throw new PlanLimitExceededException(limit);
        }
    }
}
