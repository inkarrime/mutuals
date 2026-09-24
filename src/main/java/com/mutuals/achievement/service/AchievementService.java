package com.mutuals.achievement.service;

import com.mutuals.achievement.dto.AchievementResponse;
import com.mutuals.achievement.entity.Achievement;
import com.mutuals.achievement.entity.AchievementType;
import com.mutuals.achievement.entity.UserAchievement;
import com.mutuals.achievement.repository.AchievementRepository;
import com.mutuals.achievement.repository.UserAchievementRepository;
import com.mutuals.economy.service.WalletService;
import com.mutuals.event.AchievementUnlockedEvent;
import com.mutuals.security.CurrentUserService;
import com.mutuals.user.entity.User;
import com.mutuals.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AchievementService {

    private final AchievementRepository achievementRepository;
    private final UserAchievementRepository userAchievementRepository;
    private final UserRepository userRepository;
    private final WalletService walletService;
    private final CurrentUserService currentUserService;
    private final ApplicationEventPublisher eventPublisher;
    private final Clock clock;

    @Transactional
    public int evaluate(Long userId, AchievementType type, int value) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return 0;
        }
        int unlocked = 0;
        for (Achievement achievement : achievementRepository.findByTypeAndThresholdLessThanEqual(type, value)) {
            if (!userAchievementRepository.existsByUserIdAndAchievementId(userId, achievement.getId())) {
                unlock(user, achievement);
                unlocked++;
            }
        }
        return unlocked;
    }

    @Transactional(readOnly = true)
    public List<AchievementResponse> catalogForMe() {
        Long userId = currentUserService.getCurrentUserId();
        Map<Long, UserAchievement> owned = userAchievementRepository.findAllByUserWithAchievement(userId).stream()
                .collect(Collectors.toMap(ua -> ua.getAchievement().getId(), Function.identity()));
        return achievementRepository.findAll().stream()
                .sorted(Comparator.comparing(Achievement::getType).thenComparingInt(Achievement::getThreshold))
                .map(achievement -> toResponse(achievement, owned.get(achievement.getId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AchievementResponse> unlockedBy(Long userId) {
        return userAchievementRepository.findAllByUserWithAchievement(userId).stream()
                .map(ua -> toResponse(ua.getAchievement(), ua))
                .toList();
    }

    private void unlock(User user, Achievement achievement) {
        UserAchievement userAchievement = new UserAchievement();
        userAchievement.setUser(user);
        userAchievement.setAchievement(achievement);
        userAchievement.setUnlockedAt(clock.instant());
        userAchievementRepository.save(userAchievement);
        walletService.earnGems(user, achievement.getRewardGems(), "Achievement unlocked: " + achievement.getName());
        eventPublisher.publishEvent(new AchievementUnlockedEvent(user.getId(), achievement.getId()));
    }

    private AchievementResponse toResponse(Achievement achievement, UserAchievement owned) {
        return new AchievementResponse(
                achievement.getCode(),
                achievement.getName(),
                achievement.getDescription(),
                achievement.getType(),
                achievement.getThreshold(),
                achievement.getRewardGems(),
                achievement.getIconUrl(),
                owned != null,
                owned == null ? null : owned.getUnlockedAt());
    }
}
