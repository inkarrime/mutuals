package com.mutuals.scheduler;

import com.mutuals.config.AppProperties;
import com.mutuals.subscription.service.SubscriptionService;
import com.mutuals.user.service.LocationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;

@Slf4j
@Component
@RequiredArgsConstructor
public class MaintenanceJobs {

    private static final String ZONE = "America/Lima";

    private final LocationService locationService;
    private final SubscriptionService subscriptionService;
    private final AppProperties properties;
    private final Clock clock;

    @Scheduled(cron = "0 */5 * * * *", zone = ZONE)
    public void purgeStaleLocations() {
        locationService.purgeOlderThan(clock.instant().minus(Duration.ofMinutes(properties.proximity().locationTtlMinutes())));
    }

    @Scheduled(cron = "0 0 1 * * *", zone = ZONE)
    public void expireSubscriptions() {
        log.info("{} subscriptions expired", subscriptionService.expireDue());
    }

    @Scheduled(cron = "0 0 2 1 * *", zone = ZONE)
    public void grantMonthlyShields() {
        log.info("Monthly shield granted to {} Plus subscribers", subscriptionService.grantMonthlyShields());
    }
}
