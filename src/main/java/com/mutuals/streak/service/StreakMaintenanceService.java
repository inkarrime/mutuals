package com.mutuals.streak.service;

import com.mutuals.event.ShieldUsedEvent;
import com.mutuals.event.StreakAtRiskEvent;
import com.mutuals.social.entity.Mutual;
import com.mutuals.streak.entity.Streak;
import com.mutuals.streak.entity.StreakEndReason;
import com.mutuals.streak.entity.StreakStatus;
import com.mutuals.streak.repository.StreakRepository;
import com.mutuals.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class StreakMaintenanceService {

    private final StreakRepository streakRepository;
    private final UserRepository userRepository;
    private final StreakTerminationService terminationService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public DayCloseResult closeDay(LocalDate today) {
        LocalDate yesterday = today.minusDays(1);
        List<Streak> missed = streakRepository.findMissed(StreakStatus.ACTIVE, yesterday);
        int shielded = 0;
        int broken = 0;
        for (Streak streak : missed) {
            if (streak.isShieldArmed() && streak.getCurrentLength() > 0) {
                consumeShield(streak, yesterday);
                shielded++;
            } else {
                terminationService.breakStreak(streak, StreakEndReason.MISSED_DAY, yesterday);
                broken++;
            }
        }
        int personalReset = userRepository.resetStalePersonalStreaks(yesterday);
        log.info("Day closed for {}: {} streaks protected by shields, {} broken, {} personal streaks reset",
                yesterday, shielded, broken, personalReset);
        return new DayCloseResult(shielded, broken, personalReset);
    }

    @Transactional(readOnly = true)
    public int warnAtRisk(LocalDate today) {
        List<Streak> atRisk = streakRepository.findAtRisk(StreakStatus.ACTIVE, today.minusDays(1));
        atRisk.forEach(streak -> {
            Mutual mutual = streak.getMutual();
            eventPublisher.publishEvent(new StreakAtRiskEvent(streak.getId(), mutual.getUserA().getId(),
                    mutual.getUserB().getId(), streak.getCurrentLength()));
        });
        return atRisk.size();
    }

    private void consumeShield(Streak streak, LocalDate coveredDay) {
        streak.setShieldsUsed(streak.getShieldsUsed() + 1);
        streak.setShieldArmed(false);
        streak.setShieldArmedBy(null);
        streak.setLastShieldUsedOn(coveredDay);
        streak.setLastActiveDate(coveredDay);
        Mutual mutual = streak.getMutual();
        eventPublisher.publishEvent(new ShieldUsedEvent(streak.getId(), mutual.getUserA().getId(),
                mutual.getUserB().getId(), streak.getCurrentLength()));
    }

    public record DayCloseResult(int shielded, int broken, int personalStreaksReset) {
    }
}
