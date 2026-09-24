package com.mutuals.scheduler;

import com.mutuals.challenge.service.ChallengeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChallengeJobs {

    private final ChallengeService challengeService;

    @Scheduled(cron = "0 10 0 * * MON", zone = "America/Lima")
    public void assignWeeklyChallenges() {
        LocalDate weekStart = challengeService.currentWeekStart();
        int expired = challengeService.expirePreviousWeeks(weekStart);
        int assigned = challengeService.assignWeeklyChallenges(weekStart);
        log.info("Weekly challenges: {} expired, {} assigned for week {}", expired, assigned, weekStart);
    }
}
