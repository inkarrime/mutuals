package com.mutuals.streak.service;

import com.mutuals.common.exception.ConflictException;
import com.mutuals.common.exception.ForbiddenOperationException;
import com.mutuals.common.exception.InvalidOperationException;
import com.mutuals.common.exception.ResourceNotFoundException;
import com.mutuals.economy.service.WalletService;
import com.mutuals.security.CurrentUserService;
import com.mutuals.social.entity.Mutual;
import com.mutuals.social.repository.BlockRepository;
import com.mutuals.social.service.MutualService;
import com.mutuals.streak.dto.StreakDetailResponse;
import com.mutuals.streak.dto.StreakHistoryResponse;
import com.mutuals.streak.entity.Streak;
import com.mutuals.streak.entity.StreakStatus;
import com.mutuals.streak.mapper.StreakMapper;
import com.mutuals.streak.repository.StreakRepository;
import com.mutuals.user.entity.User;
import com.mutuals.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class StreakService {

    private final StreakRepository streakRepository;
    private final BlockRepository blockRepository;
    private final MutualService mutualService;
    private final WalletService walletService;
    private final CurrentUserService currentUserService;
    private final StreakMapper streakMapper;
    private final UserMapper userMapper;
    private final Clock clock;

    @Transactional(readOnly = true)
    public Streak requireActiveStreakWith(Long userId, Long friendId) {
        Mutual mutual = mutualService.getActiveBetween(userId, friendId);
        return streakRepository.findFirstByMutualIdAndStatus(mutual.getId(), StreakStatus.ACTIVE)
                .orElseThrow(() -> new InvalidOperationException(
                        "There is no active streak with this user; send a streak invitation first"));
    }

    @Transactional(readOnly = true)
    public Streak requireMemberStreak(Long streakId, Long userId) {
        Streak streak = streakRepository.findById(streakId)
                .orElseThrow(() -> new ResourceNotFoundException("Streak", streakId));
        if (!streak.getMutual().involves(userId)) {
            throw new ForbiddenOperationException("You are not part of this streak");
        }
        return streak;
    }

    @Transactional(readOnly = true)
    public StreakDetailResponse getDetail(Long streakId) {
        Long userId = currentUserService.getCurrentUserId();
        return streakMapper.toDetail(requireMemberStreak(streakId, userId), userId, LocalDate.now(clock));
    }

    @Transactional
    public StreakDetailResponse armShield(Long streakId) {
        User me = currentUserService.getCurrentUser();
        Streak streak = requireMemberStreak(streakId, me.getId());
        LocalDate today = LocalDate.now(clock);
        if (!streak.isActive()) {
            throw new InvalidOperationException("Shields can only protect active streaks");
        }
        if (streak.isShieldArmed()) {
            throw new ConflictException("This streak already has an armed shield");
        }
        LocalDate weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        if (streak.getLastShieldUsedOn() != null && !streak.getLastShieldUsedOn().isBefore(weekStart)) {
            throw new InvalidOperationException("Only one shield per streak per week");
        }
        walletService.useShield(me, "Shield armed on streak " + streak.getId());
        streak.setShieldArmed(true);
        streak.setShieldArmedBy(me);
        return streakMapper.toDetail(streak, me.getId(), today);
    }

    @Transactional(readOnly = true)
    public List<StreakHistoryResponse> history(boolean includeBlocked) {
        Long userId = currentUserService.getCurrentUserId();
        LocalDate today = LocalDate.now(clock);
        Set<Long> hidden = includeBlocked ? Set.of() : Set.copyOf(blockRepository.findBlockedIds(userId));
        Map<Long, List<Streak>> byMutual = new LinkedHashMap<>();
        for (Streak streak : streakRepository.findAllByUser(userId)) {
            if (!hidden.contains(streak.getMutual().other(userId).getId())) {
                byMutual.computeIfAbsent(streak.getMutual().getId(), key -> new ArrayList<>()).add(streak);
            }
        }
        return byMutual.values().stream()
                .map(streaks -> toHistory(streaks, userId, today))
                .sorted(Comparator.comparingInt(StreakHistoryResponse::bestLength).reversed())
                .toList();
    }

    private StreakHistoryResponse toHistory(List<Streak> streaks, Long userId, LocalDate today) {
        Mutual mutual = streaks.get(0).getMutual();
        int best = streaks.stream().mapToInt(Streak::getCurrentLength).max().orElse(0);
        return new StreakHistoryResponse(
                userMapper.toSummary(mutual.other(userId)),
                mutual.isActive(),
                streaks.size(),
                best,
                streaks.stream().map(streak -> streakMapper.toDetail(streak, userId, today)).toList());
    }
}
