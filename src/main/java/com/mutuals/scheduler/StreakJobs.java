package com.mutuals.scheduler;

import com.mutuals.streak.service.InteractionService;
import com.mutuals.streak.service.StreakMaintenanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDate;

@Slf4j
@Component
@RequiredArgsConstructor
public class StreakJobs {

    private static final String ZONE = "America/Lima";

    private final StreakMaintenanceService maintenanceService;
    private final InteractionService interactionService;
    private final Clock clock;

    @Scheduled(cron = "0 5 0 * * *", zone = ZONE)
    public void closeDay() {
        maintenanceService.closeDay(LocalDate.now(clock));
    }

    @Scheduled(cron = "0 0 20 * * *", zone = ZONE)
    public void warnStreaksAtRisk() {
        log.info("{} streaks at risk notified", maintenanceService.warnAtRisk(LocalDate.now(clock)));
    }

    @Scheduled(cron = "0 */15 * * * *", zone = ZONE)
    public void expirePendingInteractions() {
        int expired = interactionService.expirePending();
        if (expired > 0) {
            log.info("{} pending interactions expired", expired);
        }
    }
}
