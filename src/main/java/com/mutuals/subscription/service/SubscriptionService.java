package com.mutuals.subscription.service;

import com.mutuals.common.exception.ConflictException;
import com.mutuals.common.exception.ResourceNotFoundException;
import com.mutuals.config.AppProperties;
import com.mutuals.economy.service.WalletService;
import com.mutuals.event.SubscriptionActivatedEvent;
import com.mutuals.security.CurrentUserService;
import com.mutuals.subscription.dto.BillingPeriod;
import com.mutuals.subscription.dto.SubscriptionResponse;
import com.mutuals.subscription.entity.Subscription;
import com.mutuals.subscription.entity.SubscriptionPlan;
import com.mutuals.subscription.entity.SubscriptionStatus;
import com.mutuals.subscription.repository.SubscriptionRepository;
import com.mutuals.user.entity.Role;
import com.mutuals.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private static final Set<SubscriptionStatus> CURRENT = EnumSet.of(SubscriptionStatus.ACTIVE, SubscriptionStatus.CANCELED);
    private static final List<String> PLUS_BENEFITS = List.of(
            "Unlimited active streaks",
            "1 free shield every month",
            "Recover a broken streak once a month",
            "Full all-time Wrapped by friend",
            "Exclusive avatar items",
            "Profile themes, banner, fire styles and app icons",
            "Home screen widgets");
    private static final List<String> FREE_BENEFITS = List.of(
            "Unlimited followers and mutuals",
            "Up to 4 active streaks",
            "Monthly and yearly Wrapped");

    private final SubscriptionRepository subscriptionRepository;
    private final WalletService walletService;
    private final CurrentUserService currentUserService;
    private final AppProperties properties;
    private final ApplicationEventPublisher eventPublisher;
    private final Clock clock;

    @Transactional(readOnly = true)
    public SubscriptionResponse current() {
        return subscriptionRepository.findFirstByUserIdAndStatusInOrderByExpiresAtDesc(
                        currentUserService.getCurrentUserId(), CURRENT)
                .map(this::toResponse)
                .orElseGet(this::freePlan);
    }

    @Transactional
    public SubscriptionResponse subscribe(BillingPeriod billingPeriod) {
        User me = currentUserService.getCurrentUser();
        if (subscriptionRepository.findFirstByUserIdAndStatusInOrderByExpiresAtDesc(me.getId(),
                EnumSet.of(SubscriptionStatus.ACTIVE)).isPresent()) {
            throw new ConflictException("You already have an active Mutuals Plus subscription");
        }
        Instant now = clock.instant();
        Subscription subscription = new Subscription();
        subscription.setUser(me);
        subscription.setPlan(SubscriptionPlan.PLUS);
        subscription.setStartedAt(now);
        subscription.setExpiresAt(now.atZone(ZoneId.of(properties.zone()))
                .plusMonths(billingPeriod.getMonths()).toInstant());
        subscription.setPaymentReference("SIMULATED-" + UUID.randomUUID());
        Subscription saved = subscriptionRepository.save(subscription);
        me.getRoles().add(Role.PREMIUM);
        walletService.grantShields(me, 1, "Mutuals Plus welcome shield");
        eventPublisher.publishEvent(new SubscriptionActivatedEvent(saved.getId(), me.getId(), saved.getExpiresAt()));
        return toResponse(saved);
    }

    @Transactional
    public SubscriptionResponse cancel() {
        Subscription subscription = subscriptionRepository.findFirstByUserIdAndStatusInOrderByExpiresAtDesc(
                        currentUserService.getCurrentUserId(), EnumSet.of(SubscriptionStatus.ACTIVE))
                .orElseThrow(() -> new ResourceNotFoundException("You have no active subscription"));
        subscription.setStatus(SubscriptionStatus.CANCELED);
        subscription.setCanceledAt(clock.instant());
        return toResponse(subscription);
    }

    @Transactional
    public int expireDue() {
        List<Subscription> due = subscriptionRepository.findByStatusInAndExpiresAtBefore(CURRENT, clock.instant());
        due.forEach(subscription -> {
            subscription.setStatus(SubscriptionStatus.EXPIRED);
            subscription.getUser().getRoles().remove(Role.PREMIUM);
        });
        return due.size();
    }

    @Transactional
    public int grantMonthlyShields() {
        List<Subscription> active = subscriptionRepository.findByStatusAndPlan(SubscriptionStatus.ACTIVE,
                SubscriptionPlan.PLUS);
        active.forEach(subscription -> walletService.grantShields(subscription.getUser(), 1, "Mutuals Plus monthly shield"));
        return active.size();
    }

    private SubscriptionResponse toResponse(Subscription subscription) {
        return new SubscriptionResponse(subscription.getPlan(), subscription.getStatus(), subscription.getStartedAt(),
                subscription.getExpiresAt(), Integer.MAX_VALUE, PLUS_BENEFITS);
    }

    private SubscriptionResponse freePlan() {
        return new SubscriptionResponse(SubscriptionPlan.FREE, SubscriptionStatus.ACTIVE, null, null,
                properties.streak().maxActiveStreaksFree(), FREE_BENEFITS);
    }
}
