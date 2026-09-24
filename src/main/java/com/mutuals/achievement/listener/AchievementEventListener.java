package com.mutuals.achievement.listener;

import com.mutuals.achievement.entity.AchievementType;
import com.mutuals.achievement.service.AchievementService;
import com.mutuals.challenge.entity.ChallengeStatus;
import com.mutuals.challenge.repository.ChallengeSubmissionRepository;
import com.mutuals.event.ChallengeCompletedEvent;
import com.mutuals.event.InteractionConfirmedEvent;
import com.mutuals.event.MutualCreatedEvent;
import com.mutuals.event.PersonalStreakUpdatedEvent;
import com.mutuals.social.entity.MutualStatus;
import com.mutuals.social.repository.MutualRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class AchievementEventListener {

    private final AchievementService achievementService;
    private final ChallengeSubmissionRepository submissionRepository;
    private final MutualRepository mutualRepository;

    @Async
    @TransactionalEventListener(fallbackExecution = true)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onInteractionConfirmed(InteractionConfirmedEvent event) {
        if (!event.countedToday()) {
            return;
        }
        achievementService.evaluate(event.initiatorId(), AchievementType.STREAK, event.streakLength());
        achievementService.evaluate(event.confirmerId(), AchievementType.STREAK, event.streakLength());
    }

    @Async
    @TransactionalEventListener(fallbackExecution = true)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onPersonalStreak(PersonalStreakUpdatedEvent event) {
        achievementService.evaluate(event.userId(), AchievementType.PERSONAL_STREAK, event.personalStreak());
    }

    @Async
    @TransactionalEventListener(fallbackExecution = true)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onChallengeCompleted(ChallengeCompletedEvent event) {
        evaluateChallenges(event.userAId());
        evaluateChallenges(event.userBId());
    }

    @Async
    @TransactionalEventListener(fallbackExecution = true)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onMutualCreated(MutualCreatedEvent event) {
        evaluateMutuals(event.userAId());
        evaluateMutuals(event.userBId());
    }

    private void evaluateChallenges(Long userId) {
        int completed = (int) submissionRepository.countByUserIdAndChallengeStatus(userId, ChallengeStatus.COMPLETED);
        achievementService.evaluate(userId, AchievementType.CHALLENGE, completed);
    }

    private void evaluateMutuals(Long userId) {
        int mutuals = mutualRepository.findByUserAndStatus(userId, MutualStatus.ACTIVE).size();
        achievementService.evaluate(userId, AchievementType.SOCIAL, mutuals);
    }
}
