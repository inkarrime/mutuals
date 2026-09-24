package com.mutuals.streak.service;

import com.mutuals.economy.service.WalletService;
import com.mutuals.event.StreakBrokenEvent;
import com.mutuals.social.entity.Mutual;
import com.mutuals.streak.entity.InvitationStatus;
import com.mutuals.streak.entity.Streak;
import com.mutuals.streak.entity.StreakEndReason;
import com.mutuals.streak.entity.StreakStatus;
import com.mutuals.streak.repository.StreakInvitationRepository;
import com.mutuals.streak.repository.StreakRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class StreakTerminationService {

    private final StreakRepository streakRepository;
    private final StreakInvitationRepository invitationRepository;
    private final WalletService walletService;
    private final ApplicationEventPublisher eventPublisher;
    private final Clock clock;

    @Transactional
    public void breakActiveStreak(Mutual mutual, StreakEndReason reason) {
        streakRepository.findFirstByMutualIdAndStatus(mutual.getId(), StreakStatus.ACTIVE)
                .ifPresent(streak -> breakStreak(streak, reason, LocalDate.now(clock)));
        invitationRepository.findFirstByMutualIdAndStatus(mutual.getId(), InvitationStatus.PENDING)
                .ifPresent(invitation -> invitation.setStatus(InvitationStatus.CANCELED));
    }

    @Transactional
    public void breakStreak(Streak streak, StreakEndReason reason, LocalDate endDate) {
        streak.setStatus(StreakStatus.BROKEN);
        streak.setEndReason(reason);
        streak.setEndDate(endDate);
        if (streak.isShieldArmed() && streak.getShieldArmedBy() != null) {
            walletService.grantShields(streak.getShieldArmedBy(), 1, "Armed shield refunded after streak ended");
        }
        streak.setShieldArmed(false);
        streak.setShieldArmedBy(null);
        Mutual mutual = streak.getMutual();
        eventPublisher.publishEvent(new StreakBrokenEvent(streak.getId(), mutual.getUserA().getId(),
                mutual.getUserB().getId(), streak.getCurrentLength(), reason));
    }
}
