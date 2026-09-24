package com.mutuals.streak.service;

import com.mutuals.common.dto.PageResponse;
import com.mutuals.common.exception.ConflictException;
import com.mutuals.common.exception.DuplicateResourceException;
import com.mutuals.common.exception.ForbiddenOperationException;
import com.mutuals.common.exception.InteractionExpiredException;
import com.mutuals.common.exception.ResourceNotFoundException;
import com.mutuals.config.AppProperties;
import com.mutuals.event.InteractionConfirmedEvent;
import com.mutuals.event.InteractionPendingEvent;
import com.mutuals.security.CurrentUserService;
import com.mutuals.streak.dto.CreateInteractionRequest;
import com.mutuals.streak.dto.InteractionResponse;
import com.mutuals.streak.entity.Interaction;
import com.mutuals.streak.entity.InteractionMethod;
import com.mutuals.streak.entity.InteractionStatus;
import com.mutuals.streak.entity.Streak;
import com.mutuals.streak.mapper.StreakMapper;
import com.mutuals.streak.repository.InteractionRepository;
import com.mutuals.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InteractionService {

    private final InteractionRepository interactionRepository;
    private final StreakService streakService;
    private final StreakProgressService streakProgressService;
    private final CurrentUserService currentUserService;
    private final StreakMapper streakMapper;
    private final AppProperties properties;
    private final ApplicationEventPublisher eventPublisher;
    private final Clock clock;

    @Transactional
    public InteractionResponse createManual(CreateInteractionRequest request) {
        User me = currentUserService.getCurrentUser();
        Streak streak = streakService.requireActiveStreakWith(me.getId(), request.friendId());
        if (streak.isCountedOn(LocalDate.now(clock))) {
            throw new ConflictException("Today's interaction with this friend is already confirmed");
        }
        if (interactionRepository.existsByStreakIdAndStatus(streak.getId(), InteractionStatus.PENDING)) {
            throw new DuplicateResourceException("There is already a pending interaction with this friend");
        }
        Interaction interaction = new Interaction();
        interaction.setStreak(streak);
        interaction.setInitiator(me);
        interaction.setMethod(InteractionMethod.MANUAL);
        interaction.setNote(request.note());
        interaction.setExpiresAt(clock.instant().plus(Duration.ofHours(properties.streak().manualConfirmationHours())));
        Interaction saved = interactionRepository.save(interaction);
        eventPublisher.publishEvent(new InteractionPendingEvent(saved.getId(), me.getId(), request.friendId()));
        return streakMapper.toInteraction(saved);
    }

    @Transactional(noRollbackFor = InteractionExpiredException.class)
    public InteractionResponse confirm(Long interactionId) {
        User me = currentUserService.getCurrentUser();
        Interaction interaction = requireRecipientPending(interactionId, me.getId());
        Instant now = clock.instant();
        if (interaction.getExpiresAt() != null && interaction.getExpiresAt().isBefore(now)) {
            interaction.setStatus(InteractionStatus.EXPIRED);
            throw new InteractionExpiredException(interactionId);
        }
        return streakMapper.toInteraction(complete(interaction, me));
    }

    @Transactional
    public InteractionResponse reject(Long interactionId) {
        User me = currentUserService.getCurrentUser();
        Interaction interaction = requireRecipientPending(interactionId, me.getId());
        interaction.setStatus(InteractionStatus.REJECTED);
        interaction.setConfirmer(me);
        return streakMapper.toInteraction(interaction);
    }

    @Transactional
    public Interaction recordVerified(Streak streak, User initiator, User confirmer, InteractionMethod method) {
        Interaction interaction = new Interaction();
        interaction.setStreak(streak);
        interaction.setInitiator(initiator);
        interaction.setMethod(method);
        return complete(interactionRepository.save(interaction), confirmer);
    }

    @Transactional(readOnly = true)
    public List<InteractionResponse> listPendingForMe() {
        return interactionRepository.findPendingForRecipient(currentUserService.getCurrentUserId(),
                        InteractionStatus.PENDING).stream()
                .map(streakMapper::toInteraction)
                .toList();
    }

    @Transactional(readOnly = true)
    public PageResponse<InteractionResponse> timeline(Pageable pageable) {
        return PageResponse.from(interactionRepository.findTimeline(currentUserService.getCurrentUserId(),
                InteractionStatus.CONFIRMED, pageable), streakMapper::toInteraction);
    }

    @Transactional
    public int expirePending() {
        List<Interaction> expired = interactionRepository.findByStatusAndExpiresAtBefore(InteractionStatus.PENDING,
                clock.instant());
        expired.forEach(interaction -> interaction.setStatus(InteractionStatus.EXPIRED));
        return expired.size();
    }

    private Interaction complete(Interaction interaction, User confirmer) {
        LocalDate today = LocalDate.now(clock);
        interaction.setConfirmer(confirmer);
        interaction.setStatus(InteractionStatus.CONFIRMED);
        interaction.setConfirmedAt(clock.instant());
        interaction.setInteractionDate(today);
        Streak streak = interaction.getStreak();
        boolean countedNow = streakProgressService.registerConfirmedDay(streak, today);
        eventPublisher.publishEvent(new InteractionConfirmedEvent(interaction.getId(), streak.getId(),
                interaction.getMethod(), interaction.getInitiator().getId(), confirmer.getId(),
                streak.getCurrentLength(), countedNow));
        return interaction;
    }

    private Interaction requireRecipientPending(Long interactionId, Long userId) {
        Interaction interaction = interactionRepository.findById(interactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Interaction", interactionId));
        if (!interaction.getStreak().getMutual().involves(userId)) {
            throw new ForbiddenOperationException("You are not part of this interaction");
        }
        if (interaction.getInitiator().getId().equals(userId)) {
            throw new ForbiddenOperationException("You cannot confirm or reject your own interaction");
        }
        if (!interaction.isPending()) {
            throw new InteractionExpiredException(interactionId);
        }
        return interaction;
    }
}
