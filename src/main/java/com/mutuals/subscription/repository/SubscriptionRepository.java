package com.mutuals.subscription.repository;

import com.mutuals.subscription.entity.Subscription;
import com.mutuals.subscription.entity.SubscriptionPlan;
import com.mutuals.subscription.entity.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    Optional<Subscription> findFirstByUserIdAndStatusInOrderByExpiresAtDesc(Long userId,
                                                                            Collection<SubscriptionStatus> statuses);

    List<Subscription> findByStatusInAndExpiresAtBefore(Collection<SubscriptionStatus> statuses, Instant now);

    List<Subscription> findByStatusAndPlan(SubscriptionStatus status, SubscriptionPlan plan);

    long countByStatusAndPlan(SubscriptionStatus status, SubscriptionPlan plan);
}
