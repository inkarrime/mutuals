package com.mutuals.streak.service;

import com.mutuals.common.exception.ConflictException;
import com.mutuals.common.exception.DuplicateResourceException;
import com.mutuals.common.exception.ForbiddenOperationException;
import com.mutuals.common.exception.InvalidOperationException;
import com.mutuals.common.exception.PlanLimitExceededException;
import com.mutuals.common.exception.ResourceNotFoundException;
import com.mutuals.event.StreakInvitationCreatedEvent;
import com.mutuals.event.StreakStartedEvent;
import com.mutuals.security.CurrentUserService;
import com.mutuals.social.entity.Mutual;
import com.mutuals.social.service.MutualService;
import com.mutuals.streak.dto.InvitationResponse;
import com.mutuals.streak.entity.InvitationStatus;
import com.mutuals.streak.entity.Streak;
import com.mutuals.streak.entity.StreakInvitation;
import com.mutuals.streak.entity.StreakStatus;
import com.mutuals.streak.mapper.StreakMapper;
import com.mutuals.streak.repository.StreakInvitationRepository;
import com.mutuals.streak.repository.StreakRepository;
import com.mutuals.subscription.service.PlanLimitService;
import com.mutuals.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StreakInvitationService {

    private final StreakInvitationRepository invitationRepository;
    private final StreakRepository streakRepository;
    private final MutualService mutualService;
    private final PlanLimitService planLimitService;
    private final CurrentUserService currentUserService;
    private final StreakMapper streakMapper;
    private final ApplicationEventPublisher eventPublisher;
    private final Clock clock;

    @Transactional
    public InvitationResponse invite(Long friendId) {
        User me = currentUserService.getCurrentUser();
        Mutual mutual = mutualService.getActiveBetween(me.getId(), friendId);
        if (streakRepository.findFirstByMutualIdAndStatus(mutual.getId(), StreakStatus.ACTIVE).isPresent()) {
            throw new ConflictException("You already have an active streak with this user");
        }
        if (invitationRepository.existsByMutualIdAndStatus(mutual.getId(), InvitationStatus.PENDING)) {
            throw new DuplicateResourceException("There is already a pending streak invitation");
        }
        planLimitService.assertCanStartStreak(me);
        StreakInvitation invitation = new StreakInvitation();
        invitation.setMutual(mutual);
        invitation.setInviter(me);
        invitation.setInvitee(mutual.other(me.getId()));
        StreakInvitation saved = invitationRepository.save(invitation);
        eventPublisher.publishEvent(new StreakInvitationCreatedEvent(saved.getId(), me.getId(), friendId));
        return streakMapper.toInvitation(saved);
    }

    @Transactional
    public InvitationResponse respond(Long invitationId, boolean accept) {
        User me = currentUserService.getCurrentUser();
        StreakInvitation invitation = requirePending(invitationId);
        if (!invitation.getInvitee().getId().equals(me.getId())) {
            throw new ForbiddenOperationException("Only the invited user can respond to this invitation");
        }
        invitation.setRespondedAt(clock.instant());
        if (!accept) {
            invitation.setStatus(InvitationStatus.REJECTED);
            return streakMapper.toInvitation(invitation);
        }
        if (!invitation.getMutual().isActive()) {
            throw new InvalidOperationException("You are no longer mutuals with this user");
        }
        planLimitService.assertCanStartStreak(me);
        assertInviterStillHasRoom(invitation.getInviter());
        invitation.setStatus(InvitationStatus.ACCEPTED);
        Streak streak = new Streak();
        streak.setMutual(invitation.getMutual());
        streak.setStartDate(LocalDate.now(clock));
        Streak saved = streakRepository.save(streak);
        eventPublisher.publishEvent(new StreakStartedEvent(saved.getId(), invitation.getInviter().getId(), me.getId()));
        return streakMapper.toInvitation(invitation);
    }

    @Transactional
    public void cancel(Long invitationId) {
        StreakInvitation invitation = requirePending(invitationId);
        if (!invitation.getInviter().getId().equals(currentUserService.getCurrentUserId())) {
            throw new ForbiddenOperationException("Only the inviter can cancel this invitation");
        }
        invitation.setStatus(InvitationStatus.CANCELED);
        invitation.setRespondedAt(clock.instant());
    }

    @Transactional(readOnly = true)
    public List<InvitationResponse> listReceived() {
        return invitationRepository.findByInviteeIdAndStatusOrderByCreatedAtDesc(
                        currentUserService.getCurrentUserId(), InvitationStatus.PENDING).stream()
                .map(streakMapper::toInvitation)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<InvitationResponse> listSent() {
        return invitationRepository.findByInviterIdAndStatusOrderByCreatedAtDesc(
                        currentUserService.getCurrentUserId(), InvitationStatus.PENDING).stream()
                .map(streakMapper::toInvitation)
                .toList();
    }

    private StreakInvitation requirePending(Long invitationId) {
        StreakInvitation invitation = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new ResourceNotFoundException("Streak invitation", invitationId));
        if (invitation.getStatus() != InvitationStatus.PENDING) {
            throw new InvalidOperationException("This invitation is no longer pending");
        }
        return invitation;
    }

    private void assertInviterStillHasRoom(User inviter) {
        try {
            planLimitService.assertCanStartStreak(inviter);
        } catch (PlanLimitExceededException ex) {
            throw new ConflictException("The inviter has reached the free plan streak limit");
        }
    }
}
