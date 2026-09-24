package com.mutuals.streak.service;

import com.mutuals.common.exception.InvalidOperationException;
import com.mutuals.config.AppProperties;
import com.mutuals.economy.service.WalletService;
import com.mutuals.event.StreakMilestoneReachedEvent;
import com.mutuals.social.entity.Mutual;
import com.mutuals.streak.entity.Streak;
import com.mutuals.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class StreakProgressService {

    private static final int DAYS_PER_WEEK = 7;

    private final PersonalStreakService personalStreakService;
    private final WalletService walletService;
    private final AppProperties properties;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public boolean registerConfirmedDay(Streak streak, LocalDate date) {
        if (!streak.isActive()) {
            throw new InvalidOperationException("Streak is no longer active");
        }
        if (streak.isCountedOn(date)) {
            return false;
        }
        streak.setCurrentLength(streak.getCurrentLength() + 1);
        streak.setLastActiveDate(date);
        Mutual mutual = streak.getMutual();
        rewardCompletedWeeks(streak, mutual.getUserA(), mutual.getUserB());
        personalStreakService.registerActivity(mutual.getUserA(), date);
        personalStreakService.registerActivity(mutual.getUserB(), date);
        if (properties.streak().milestones().contains(streak.getCurrentLength())) {
            eventPublisher.publishEvent(new StreakMilestoneReachedEvent(streak.getId(), mutual.getUserA().getId(),
                    mutual.getUserB().getId(), streak.getCurrentLength()));
        }
        return true;
    }

    private void rewardCompletedWeeks(Streak streak, User first, User second) {
        int completedWeeks = streak.getCurrentLength() / DAYS_PER_WEEK;
        if (completedWeeks <= streak.getWeeksRewarded()) {
            return;
        }
        streak.setWeeksRewarded(completedWeeks);
        int gems = properties.streak().gemsPerCompletedWeek();
        String reason = "Completed week " + completedWeeks + " of streak";
        walletService.earnGems(first, gems, reason);
        walletService.earnGems(second, gems, reason);
    }
}
