package com.mutuals.scheduler;

import com.mutuals.wrapped.dto.WrappedPeriod;
import com.mutuals.wrapped.service.WrappedGenerationService;
import com.mutuals.wrapped.service.WrappedService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Year;
import java.time.YearMonth;

@Component
@RequiredArgsConstructor
public class WrappedJobs {

    private static final String ZONE = "America/Lima";

    private final WrappedGenerationService generationService;
    private final WrappedService wrappedService;
    private final Clock clock;

    @Scheduled(cron = "0 0 3 1 * *", zone = ZONE)
    public void generateMonthly() {
        generationService.generateForAllUsers(WrappedPeriod.month(YearMonth.now(clock).minusMonths(1)));
    }

    @Scheduled(cron = "0 0 4 1 1 *", zone = ZONE)
    public void generateYearly() {
        WrappedPeriod lastYear = WrappedPeriod.year(Year.now(clock).minusYears(1));
        generationService.generateForAllUsers(lastYear);
        wrappedService.generatePlatform(lastYear);
    }
}
