package com.mutuals.activity.listener;

import com.mutuals.achievement.repository.AchievementRepository;
import com.mutuals.activity.entity.ActivityType;
import com.mutuals.activity.service.ActivityPublisher;
import com.mutuals.config.AppProperties;
import com.mutuals.event.AchievementUnlockedEvent;
import com.mutuals.event.PersonalStreakUpdatedEvent;
import com.mutuals.event.StreakMilestoneReachedEvent;
import com.mutuals.notification.entity.NotificationPriority;
import com.mutuals.notification.entity.NotificationType;
import com.mutuals.notification.service.NotificationService;
import com.mutuals.social.repository.FollowRepository;
import com.mutuals.user.entity.User;
import com.mutuals.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class ActivityEventListener {

    private static final int HIGHLIGHT_MIN_LENGTH = 30;

    private final ActivityPublisher activityPublisher;
    private final UserRepository userRepository;
    private final AchievementRepository achievementRepository;
    private final FollowRepository followRepository;
    private final NotificationService notificationService;
    private final AppProperties properties;

    @Async
    @TransactionalEventListener(fallbackExecution = true)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onStreakMilestone(StreakMilestoneReachedEvent event) {
        publishMilestone(event.userAId(), event.userBId(), event.length());
        publishMilestone(event.userBId(), event.userAId(), event.length());
    }

    @Async
    @TransactionalEventListener(fallbackExecution = true)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onPersonalStreak(PersonalStreakUpdatedEvent event) {
        if (!properties.streak().milestones().contains(event.personalStreak())) {
            return;
        }
        userRepository.findById(event.userId()).ifPresent(user -> activityPublisher.publish(user,
                ActivityType.PERSONAL_STREAK_MILESTONE,
                user.getDisplayName() + " lleva " + event.personalStreak() + " días seguidos conectando con sus amigos",
                event.personalStreak()));
    }

    @Async
    @TransactionalEventListener(fallbackExecution = true)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onAchievement(AchievementUnlockedEvent event) {
        User user = userRepository.findById(event.userId()).orElse(null);
        if (user == null) {
            return;
        }
        achievementRepository.findById(event.achievementId()).ifPresent(achievement -> activityPublisher.publish(user,
                ActivityType.ACHIEVEMENT_UNLOCKED, user.getDisplayName() + " desbloqueó " + achievement.getName(), null));
    }

    private void publishMilestone(Long userId, Long partnerId, int length) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return;
        }
        String title = user.getDisplayName() + " alcanzó " + length + " días de racha";
        var activity = activityPublisher.publish(user, ActivityType.STREAK_MILESTONE, title, length);
        if (length < HIGHLIGHT_MIN_LENGTH) {
            return;
        }
        followRepository.findFollowerIds(userId).stream()
                .filter(followerId -> !followerId.equals(partnerId))
                .forEach(followerId -> notificationService.notify(followerId, NotificationType.HIGHLIGHT,
                        NotificationPriority.LOW, title, null, "ACTIVITY", activity.getId()));
    }
}
