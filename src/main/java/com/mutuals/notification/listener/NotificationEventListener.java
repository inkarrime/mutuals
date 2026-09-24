package com.mutuals.notification.listener;

import com.mutuals.achievement.repository.AchievementRepository;
import com.mutuals.event.AchievementUnlockedEvent;
import com.mutuals.event.ActivityCommentedEvent;
import com.mutuals.event.ActivityLikedEvent;
import com.mutuals.event.ChallengeAssignedEvent;
import com.mutuals.event.ChallengeCompletedEvent;
import com.mutuals.event.InteractionConfirmedEvent;
import com.mutuals.event.InteractionPendingEvent;
import com.mutuals.event.MutualCreatedEvent;
import com.mutuals.event.ProximityDetectedEvent;
import com.mutuals.event.ShieldGiftedEvent;
import com.mutuals.event.ShieldUsedEvent;
import com.mutuals.event.StreakAtRiskEvent;
import com.mutuals.event.StreakBrokenEvent;
import com.mutuals.event.StreakInvitationCreatedEvent;
import com.mutuals.event.StreakStartedEvent;
import com.mutuals.event.SubscriptionActivatedEvent;
import com.mutuals.event.UserFollowedEvent;
import com.mutuals.event.WrappedGeneratedEvent;
import com.mutuals.activity.repository.ActivityRepository;
import com.mutuals.notification.entity.NotificationPriority;
import com.mutuals.notification.entity.NotificationType;
import com.mutuals.notification.service.NotificationService;
import com.mutuals.social.service.MutualService;
import com.mutuals.streak.entity.InteractionMethod;
import com.mutuals.streak.entity.StreakEndReason;
import com.mutuals.user.entity.User;
import com.mutuals.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.Clock;
import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class NotificationEventListener {

    private static final String REF_USER = "USER";
    private static final String REF_STREAK = "STREAK";
    private static final String REF_INTERACTION = "INTERACTION";
    private static final String REF_INVITATION = "STREAK_INVITATION";
    private static final String REF_CHALLENGE = "CHALLENGE";
    private static final String REF_ACTIVITY = "ACTIVITY";
    private static final String REF_WRAPPED = "WRAPPED";

    private final NotificationService notificationService;
    private final UserRepository userRepository;
    private final AchievementRepository achievementRepository;
    private final ActivityRepository activityRepository;
    private final MutualService mutualService;
    private final Clock clock;

    @Async
    @TransactionalEventListener(fallbackExecution = true)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onFollowed(UserFollowedEvent event) {
        notificationService.notify(event.followedId(), NotificationType.NEW_FOLLOWER, NotificationPriority.LOW,
                name(event.followerId()) + " empezó a seguirte", null, REF_USER, event.followerId());
    }

    @Async
    @TransactionalEventListener(fallbackExecution = true)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onMutualCreated(MutualCreatedEvent event) {
        notifyHigh(event.userAId(), NotificationType.MUTUAL_CREATED, "Tú y " + name(event.userBId()) + " ahora son mutuals",
                "Invítalo a su primera racha", REF_USER, event.userBId());
        notifyHigh(event.userBId(), NotificationType.MUTUAL_CREATED, "Tú y " + name(event.userAId()) + " ahora son mutuals",
                "Invítalo a su primera racha", REF_USER, event.userAId());
    }

    @Async
    @TransactionalEventListener(fallbackExecution = true)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onInvitation(StreakInvitationCreatedEvent event) {
        notifyHigh(event.inviteeId(), NotificationType.STREAK_INVITATION,
                name(event.inviterId()) + " quiere empezar una racha contigo", "Acepta y empiecen hoy",
                REF_INVITATION, event.invitationId());
    }

    @Async
    @TransactionalEventListener(fallbackExecution = true)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onStreakStarted(StreakStartedEvent event) {
        notifyHigh(event.inviterId(), NotificationType.STREAK_STARTED,
                name(event.inviteeId()) + " aceptó tu racha", "Registren su primer momento hoy", REF_STREAK, event.streakId());
    }

    @Async
    @TransactionalEventListener(fallbackExecution = true)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onInteractionPending(InteractionPendingEvent event) {
        notifyHigh(event.recipientId(), NotificationType.INTERACTION_PENDING,
                name(event.initiatorId()) + " marcó que hablaron hoy", "Confírmalo para sumar a su racha",
                REF_INTERACTION, event.interactionId());
    }

    @Async
    @TransactionalEventListener(fallbackExecution = true)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onInteractionConfirmed(InteractionConfirmedEvent event) {
        if (!event.countedToday()) {
            return;
        }
        String body = "Llevan " + days(event.streakLength()) + " de racha";
        notifyHigh(event.initiatorId(), NotificationType.INTERACTION_CONFIRMED,
                name(event.confirmerId()) + " confirmó su momento", body, REF_STREAK, event.streakId());
        if (event.method() != InteractionMethod.MANUAL) {
            notifyHigh(event.confirmerId(), NotificationType.INTERACTION_CONFIRMED,
                    "Check-in con " + name(event.initiatorId()) + " confirmado", body, REF_STREAK, event.streakId());
        }
    }

    @Async
    @TransactionalEventListener(fallbackExecution = true)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onProximity(ProximityDetectedEvent event) {
        notifyProximityOnce(event.userAId(), event.userBId(), event.streakId());
        notifyProximityOnce(event.userBId(), event.userAId(), event.streakId());
    }

    @Async
    @TransactionalEventListener(fallbackExecution = true)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onAtRisk(StreakAtRiskEvent event) {
        String body = "Su racha de " + days(event.length()) + " vence a medianoche";
        notifyHigh(event.userAId(), NotificationType.STREAK_AT_RISK, "Tu racha con " + name(event.userBId()) + " está en riesgo",
                body, REF_STREAK, event.streakId());
        notifyHigh(event.userBId(), NotificationType.STREAK_AT_RISK, "Tu racha con " + name(event.userAId()) + " está en riesgo",
                body, REF_STREAK, event.streakId());
    }

    @Async
    @TransactionalEventListener(fallbackExecution = true)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onBroken(StreakBrokenEvent event) {
        if (event.reason() != StreakEndReason.MISSED_DAY || event.length() == 0) {
            return;
        }
        String body = "Llegaron a " + days(event.length()) + ". Pueden empezar de nuevo cuando quieran";
        notifyHigh(event.userAId(), NotificationType.STREAK_BROKEN, "Tu racha con " + name(event.userBId()) + " terminó",
                body, REF_STREAK, event.streakId());
        notifyHigh(event.userBId(), NotificationType.STREAK_BROKEN, "Tu racha con " + name(event.userAId()) + " terminó",
                body, REF_STREAK, event.streakId());
    }

    @Async
    @TransactionalEventListener(fallbackExecution = true)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onShieldUsed(ShieldUsedEvent event) {
        String body = "Su racha de " + days(event.length()) + " sigue viva. Hablen hoy";
        notifyHigh(event.userAId(), NotificationType.SHIELD_USED, "Un escudo protegió tu racha con " + name(event.userBId()),
                body, REF_STREAK, event.streakId());
        notifyHigh(event.userBId(), NotificationType.SHIELD_USED, "Un escudo protegió tu racha con " + name(event.userAId()),
                body, REF_STREAK, event.streakId());
    }

    @Async
    @TransactionalEventListener(fallbackExecution = true)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onShieldGifted(ShieldGiftedEvent event) {
        notifyHigh(event.receiverId(), NotificationType.SHIELD_RECEIVED, name(event.senderId()) + " te regaló un escudo",
                "Úsalo para proteger una racha", REF_USER, event.senderId());
    }

    @Async
    @TransactionalEventListener(fallbackExecution = true)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onChallengeAssigned(ChallengeAssignedEvent event) {
        notifyHigh(event.userAId(), NotificationType.CHALLENGE_ASSIGNED, "Nuevo desafío con " + name(event.userBId()),
                "Tienen toda la semana para completarlo", REF_CHALLENGE, event.challengeId());
        notifyHigh(event.userBId(), NotificationType.CHALLENGE_ASSIGNED, "Nuevo desafío con " + name(event.userAId()),
                "Tienen toda la semana para completarlo", REF_CHALLENGE, event.challengeId());
    }

    @Async
    @TransactionalEventListener(fallbackExecution = true)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onChallengeCompleted(ChallengeCompletedEvent event) {
        String body = "Ya pueden ver sus respuestas. +" + event.rewardGems() + " gemas";
        notifyHigh(event.userAId(), NotificationType.CHALLENGE_COMPLETED, "Desafío completado con " + name(event.userBId()),
                body, REF_CHALLENGE, event.challengeId());
        notifyHigh(event.userBId(), NotificationType.CHALLENGE_COMPLETED, "Desafío completado con " + name(event.userAId()),
                body, REF_CHALLENGE, event.challengeId());
    }

    @Async
    @TransactionalEventListener(fallbackExecution = true)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onAchievement(AchievementUnlockedEvent event) {
        achievementRepository.findById(event.achievementId()).ifPresent(achievement ->
                notifyHigh(event.userId(), NotificationType.ACHIEVEMENT_UNLOCKED, "Logro desbloqueado: " + achievement.getName(),
                        achievement.getRewardGems() > 0 ? "+" + achievement.getRewardGems() + " gemas" : null,
                        "ACHIEVEMENT", achievement.getId()));
    }

    @Async
    @TransactionalEventListener(fallbackExecution = true)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onLiked(ActivityLikedEvent event) {
        activityRepository.findById(event.activityId()).ifPresent(activity -> {
            Long actorId = activity.getActor().getId();
            if (actorId.equals(event.likerId())) {
                return;
            }
            NotificationPriority priority = mutualService.findActiveBetween(actorId, event.likerId()).isPresent()
                    ? NotificationPriority.HIGH : NotificationPriority.LOW;
            notificationService.notify(actorId, NotificationType.ACTIVITY_LIKED, priority,
                    "A " + name(event.likerId()) + " le gustó tu logro", activity.getTitle(), REF_ACTIVITY, activity.getId());
        });
    }

    @Async
    @TransactionalEventListener(fallbackExecution = true)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onCommented(ActivityCommentedEvent event) {
        activityRepository.findById(event.activityId())
                .filter(activity -> !activity.getActor().getId().equals(event.authorId()))
                .ifPresent(activity -> notifyHigh(activity.getActor().getId(), NotificationType.ACTIVITY_COMMENTED,
                        name(event.authorId()) + " comentó tu logro", activity.getTitle(), REF_ACTIVITY, activity.getId()));
    }

    @Async
    @TransactionalEventListener(fallbackExecution = true)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onWrapped(WrappedGeneratedEvent event) {
        notifyHigh(event.userId(), NotificationType.WRAPPED_READY, "Tu Wrapped " + event.periodKey() + " está listo",
                "Descubre con quién conectaste más y compártelo", REF_WRAPPED, event.wrappedId());
    }

    @Async
    @TransactionalEventListener(fallbackExecution = true)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onSubscription(SubscriptionActivatedEvent event) {
        notifyHigh(event.userId(), NotificationType.SUBSCRIPTION, "Bienvenido a Mutuals Plus",
                "Rachas ilimitadas y un escudo de regalo ya están en tu cuenta", "SUBSCRIPTION", event.subscriptionId());
    }

    private void notifyProximityOnce(Long recipientId, Long friendId, Long streakId) {
        if (notificationService.wasNotifiedSince(recipientId, NotificationType.PROXIMITY_DETECTED, streakId,
                LocalDate.now(clock).atStartOfDay(clock.getZone()).toInstant())) {
            return;
        }
        notifyHigh(recipientId, NotificationType.PROXIMITY_DETECTED, "Parece que estás con " + name(friendId),
                "Hagan check-in para registrar el momento", REF_STREAK, streakId);
    }

    private void notifyHigh(Long recipientId, NotificationType type, String title, String body, String referenceType,
                            Long referenceId) {
        notificationService.notify(recipientId, type, NotificationPriority.HIGH, title, body, referenceType, referenceId);
    }

    private static String days(int count) {
        return count == 1 ? "1 día" : count + " días";
    }

    private String name(Long userId) {
        return userRepository.findById(userId).map(User::getDisplayName).orElse("Alguien");
    }
}
