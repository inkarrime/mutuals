package com.mutuals.social.service;

import com.mutuals.common.exception.ForbiddenOperationException;
import com.mutuals.common.util.PairKey;
import com.mutuals.event.MutualCreatedEvent;
import com.mutuals.security.CurrentUserService;
import com.mutuals.social.dto.MutualCardResponse;
import com.mutuals.social.entity.Mutual;
import com.mutuals.social.entity.MutualEndReason;
import com.mutuals.social.entity.MutualStatus;
import com.mutuals.social.repository.MutualRepository;
import com.mutuals.streak.dto.StreakCardResponse;
import com.mutuals.streak.entity.InvitationStatus;
import com.mutuals.streak.entity.StreakEndReason;
import com.mutuals.streak.entity.StreakStatus;
import com.mutuals.streak.mapper.StreakMapper;
import com.mutuals.streak.repository.StreakInvitationRepository;
import com.mutuals.streak.repository.StreakRepository;
import com.mutuals.streak.service.StreakTerminationService;
import com.mutuals.user.entity.User;
import com.mutuals.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MutualService {

    private final MutualRepository mutualRepository;
    private final StreakRepository streakRepository;
    private final StreakInvitationRepository invitationRepository;
    private final StreakTerminationService streakTerminationService;
    private final CurrentUserService currentUserService;
    private final StreakMapper streakMapper;
    private final UserMapper userMapper;
    private final ApplicationEventPublisher eventPublisher;
    private final Clock clock;

    @Transactional(readOnly = true)
    public Optional<Mutual> findActiveBetween(Long firstUserId, Long secondUserId) {
        PairKey key = PairKey.of(firstUserId, secondUserId);
        return mutualRepository.findByUserAIdAndUserBId(key.lowId(), key.highId()).filter(Mutual::isActive);
    }

    @Transactional(readOnly = true)
    public Mutual getActiveBetween(Long firstUserId, Long secondUserId) {
        return findActiveBetween(firstUserId, secondUserId)
                .orElseThrow(() -> new ForbiddenOperationException("This action is only available between mutuals"));
    }

    @Transactional
    public Mutual activate(User first, User second) {
        PairKey key = PairKey.of(first.getId(), second.getId());
        Instant now = clock.instant();
        Mutual mutual = mutualRepository.findByUserAIdAndUserBId(key.lowId(), key.highId()).orElseGet(() -> {
            Mutual created = new Mutual();
            created.setUserA(first.getId().equals(key.lowId()) ? first : second);
            created.setUserB(first.getId().equals(key.lowId()) ? second : first);
            created.setFirstMutualAt(now);
            return created;
        });
        mutual.setStatus(MutualStatus.ACTIVE);
        mutual.setCurrentSince(now);
        mutual.setEndedAt(null);
        mutual.setEndReason(null);
        Mutual saved = mutualRepository.save(mutual);
        eventPublisher.publishEvent(new MutualCreatedEvent(saved.getId(), saved.getUserA().getId(), saved.getUserB().getId()));
        return saved;
    }

    @Transactional
    public void end(Mutual mutual, MutualEndReason reason) {
        if (!mutual.isActive()) {
            return;
        }
        mutual.setStatus(MutualStatus.ENDED);
        mutual.setEndedAt(clock.instant());
        mutual.setEndReason(reason);
        streakTerminationService.breakActiveStreak(mutual, toStreakReason(reason));
    }

    @Transactional
    public void endIfActive(Long firstUserId, Long secondUserId, MutualEndReason reason) {
        findActiveBetween(firstUserId, secondUserId).ifPresent(mutual -> end(mutual, reason));
    }

    @Transactional(readOnly = true)
    public List<MutualCardResponse> listMyMutuals() {
        Long userId = currentUserService.getCurrentUserId();
        LocalDate today = LocalDate.now(clock);
        return mutualRepository.findByUserAndStatus(userId, MutualStatus.ACTIVE).stream()
                .map(mutual -> toCard(mutual, userId, today))
                .sorted(homeOrder())
                .toList();
    }

    private MutualCardResponse toCard(Mutual mutual, Long userId, LocalDate today) {
        StreakCardResponse streak = streakRepository.findFirstByMutualIdAndStatus(mutual.getId(), StreakStatus.ACTIVE)
                .map(active -> streakMapper.toCard(active, today))
                .orElse(null);
        boolean invitationPending = invitationRepository.existsByMutualIdAndStatus(mutual.getId(), InvitationStatus.PENDING);
        return new MutualCardResponse(mutual.getId(), userMapper.toSummary(mutual.other(userId)),
                mutual.getCurrentSince(), streak, invitationPending);
    }

    private Comparator<MutualCardResponse> homeOrder() {
        Comparator<MutualCardResponse> atRiskFirst = Comparator.comparing(
                card -> card.streak() == null || !card.streak().atRisk());
        Comparator<MutualCardResponse> longestFirst = Comparator.comparingInt(
                card -> card.streak() == null ? -1 : -card.streak().currentLength());
        return atRiskFirst.thenComparing(longestFirst);
    }

    private StreakEndReason toStreakReason(MutualEndReason reason) {
        return switch (reason) {
            case UNFOLLOW -> StreakEndReason.UNFOLLOW;
            case BLOCK -> StreakEndReason.BLOCK;
            case ACCOUNT_DELETED -> StreakEndReason.ACCOUNT_DELETED;
        };
    }
}
