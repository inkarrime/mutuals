package com.mutuals.wrapped.service;

import com.mutuals.challenge.entity.ChallengeStatus;
import com.mutuals.challenge.repository.ChallengeSubmissionRepository;
import com.mutuals.economy.entity.WalletCurrency;
import com.mutuals.economy.entity.WalletTransactionType;
import com.mutuals.economy.repository.WalletTransactionRepository;
import com.mutuals.social.entity.Mutual;
import com.mutuals.social.entity.MutualStatus;
import com.mutuals.social.repository.MutualRepository;
import com.mutuals.streak.entity.InteractionStatus;
import com.mutuals.streak.entity.Streak;
import com.mutuals.streak.repository.InteractionRepository;
import com.mutuals.streak.repository.StreakRepository;
import com.mutuals.user.entity.User;
import com.mutuals.user.entity.UserStatus;
import com.mutuals.user.mapper.UserMapper;
import com.mutuals.user.repository.UserRepository;
import com.mutuals.wrapped.dto.FriendWrappedSection;
import com.mutuals.wrapped.dto.PlatformWrappedStats;
import com.mutuals.wrapped.dto.UserWrappedStats;
import com.mutuals.wrapped.dto.WrappedPeriod;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class WrappedStatsCalculator {

    private static final InteractionStatus CONFIRMED = InteractionStatus.CONFIRMED;

    private final InteractionRepository interactionRepository;
    private final StreakRepository streakRepository;
    private final MutualRepository mutualRepository;
    private final ChallengeSubmissionRepository submissionRepository;
    private final WalletTransactionRepository walletRepository;
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserWrappedStats forUser(Long userId, WrappedPeriod period, ZoneId zone) {
        Instant from = period.fromInstant(zone);
        Instant to = period.toInstant(zone);
        List<Object[]> byMutual = interactionRepository.countByMutualForUser(userId, CONFIRMED, period.from(), period.to());
        String topUsername = null;
        String topDisplayName = null;
        long topCount = 0;
        if (!byMutual.isEmpty()) {
            Long mutualId = (Long) byMutual.get(0)[0];
            topCount = (Long) byMutual.get(0)[1];
            User top = mutualRepository.findById(mutualId).map(mutual -> mutual.other(userId)).orElse(null);
            topUsername = top == null ? null : top.getUsername();
            topDisplayName = top == null ? null : top.getDisplayName();
        }
        Long gems = walletRepository.sumAmount(userId, WalletCurrency.GEM, WalletTransactionType.EARN, from, to);
        return new UserWrappedStats(
                interactionRepository.countForUser(userId, CONFIRMED, period.from(), period.to()),
                interactionRepository.countActiveDaysForUser(userId, CONFIRMED, period.from(), period.to()),
                streakRepository.findLongestInRange(userId, period.from(), period.to()),
                toMethodMap(interactionRepository.countByMethodForUser(userId, CONFIRMED, period.from(), period.to())),
                topUsername,
                topDisplayName,
                topCount,
                submissionRepository.countCompletedForUserBetween(userId, ChallengeStatus.COMPLETED, from, to),
                gems == null ? 0 : gems,
                walletRepository.countByUserIdAndCurrencyAndTypeAndCreatedAtBetween(userId, WalletCurrency.SHIELD,
                        WalletTransactionType.GIFT_SENT, from, to),
                mutualRepository.countStartedBetween(userId, from, to));
    }

    public List<FriendWrappedSection> friendSections(Long userId) {
        Map<Long, List<Streak>> streaksByMutual = streakRepository.findAllByUser(userId).stream()
                .collect(Collectors.groupingBy(streak -> streak.getMutual().getId()));
        return mutualRepository.findAllByUser(userId).stream()
                .map(mutual -> toSection(mutual, userId, streaksByMutual.getOrDefault(mutual.getId(), List.of())))
                .sorted(Comparator.comparingLong(FriendWrappedSection::interactions).reversed())
                .toList();
    }

    public PlatformWrappedStats forPlatform(WrappedPeriod period, ZoneId zone) {
        Instant from = period.fromInstant(zone);
        Instant to = period.toInstant(zone);
        List<Object[]> byDay = interactionRepository.countByDayPlatform(CONFIRMED, period.from(), period.to());
        return new PlatformWrappedStats(
                interactionRepository.countPlatform(CONFIRMED, period.from(), period.to()),
                toMethodMap(interactionRepository.countByMethodPlatform(CONFIRMED, period.from(), period.to())),
                streakRepository.findPlatformLongestInRange(period.from(), period.to()),
                byDay.isEmpty() ? null : byDay.get(0)[0].toString(),
                byDay.isEmpty() ? 0 : (Long) byDay.get(0)[1],
                userRepository.countByStatus(UserStatus.ACTIVE),
                mutualRepository.countByStatus(MutualStatus.ACTIVE),
                walletRepository.countByCurrencyAndTypeAndCreatedAtBetween(WalletCurrency.SHIELD,
                        WalletTransactionType.GIFT_SENT, from, to));
    }

    private FriendWrappedSection toSection(Mutual mutual, Long userId, List<Streak> streaks) {
        return new FriendWrappedSection(
                userMapper.toSummary(mutual.other(userId)),
                mutual.isActive(),
                mutual.getFirstMutualAt(),
                streaks.size(),
                streaks.stream().mapToInt(Streak::getCurrentLength).max().orElse(0),
                interactionRepository.countForMutual(mutual.getId(), CONFIRMED),
                toMethodMap(interactionRepository.countByMethodForMutual(mutual.getId(), CONFIRMED)),
                submissionRepository.countCompletedForUserAndMutual(userId, mutual.getId(), ChallengeStatus.COMPLETED));
    }

    private Map<String, Long> toMethodMap(List<Object[]> rows) {
        Map<String, Long> result = new LinkedHashMap<>();
        rows.forEach(row -> result.put(row[0].toString(), (Long) row[1]));
        return result;
    }
}
