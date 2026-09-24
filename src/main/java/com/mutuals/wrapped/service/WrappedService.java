package com.mutuals.wrapped.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mutuals.common.exception.ResourceNotFoundException;
import com.mutuals.config.AppProperties;
import com.mutuals.event.WrappedGeneratedEvent;
import com.mutuals.security.CurrentUserService;
import com.mutuals.user.entity.Role;
import com.mutuals.user.entity.User;
import com.mutuals.user.repository.UserRepository;
import com.mutuals.wrapped.dto.AllTimeWrappedResponse;
import com.mutuals.wrapped.dto.FriendWrappedSection;
import com.mutuals.wrapped.dto.UserWrappedStats;
import com.mutuals.wrapped.dto.WrappedPeriod;
import com.mutuals.wrapped.dto.WrappedResponse;
import com.mutuals.wrapped.entity.WrappedScope;
import com.mutuals.wrapped.entity.WrappedSummary;
import com.mutuals.wrapped.repository.WrappedSummaryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WrappedService {

    private static final int FREE_FRIEND_SECTIONS = 3;

    private final WrappedSummaryRepository summaryRepository;
    private final WrappedStatsCalculator calculator;
    private final UserRepository userRepository;
    private final CurrentUserService currentUserService;
    private final ObjectMapper objectMapper;
    private final AppProperties properties;
    private final ApplicationEventPublisher eventPublisher;
    private final Clock clock;

    @Transactional
    public void generateForUser(Long userId, WrappedPeriod period) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null || !user.isActive()) {
            return;
        }
        UserWrappedStats stats = calculator.forUser(userId, period, zone());
        WrappedSummary summary = summaryRepository.findByScopeAndUserIdAndPeriodTypeAndPeriodKey(
                WrappedScope.USER, userId, period.type(), period.key()).orElseGet(WrappedSummary::new);
        summary.setScope(WrappedScope.USER);
        summary.setUser(user);
        summary.setPeriodType(period.type());
        summary.setPeriodKey(period.key());
        summary.setStatsJson(toJson(stats));
        summary.setGeneratedAt(clock.instant());
        WrappedSummary saved = summaryRepository.save(summary);
        if (stats.interactions() > 0) {
            eventPublisher.publishEvent(new WrappedGeneratedEvent(saved.getId(), userId, period.key()));
        }
    }

    @Transactional
    public WrappedResponse generatePlatform(WrappedPeriod period) {
        WrappedSummary summary = summaryRepository.findByScopeAndUserIsNullAndPeriodTypeAndPeriodKey(
                WrappedScope.PLATFORM, period.type(), period.key()).orElseGet(WrappedSummary::new);
        summary.setScope(WrappedScope.PLATFORM);
        summary.setPeriodType(period.type());
        summary.setPeriodKey(period.key());
        summary.setStatsJson(toJson(calculator.forPlatform(period, zone())));
        summary.setGeneratedAt(clock.instant());
        return toResponse(summaryRepository.save(summary));
    }

    @Transactional(readOnly = true)
    public List<WrappedResponse> myWrappeds() {
        return summaryRepository.findByUserIdAndScopeOrderByGeneratedAtDesc(currentUserService.getCurrentUserId(),
                WrappedScope.USER).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public WrappedResponse myWrapped(String periodKey) {
        WrappedPeriod period = WrappedPeriod.parse(periodKey);
        return summaryRepository.findByScopeAndUserIdAndPeriodTypeAndPeriodKey(WrappedScope.USER,
                        currentUserService.getCurrentUserId(), period.type(), period.key())
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Wrapped for period " + periodKey + " is not ready yet"));
    }

    @Transactional(readOnly = true)
    public WrappedResponse platformWrapped(String periodKey) {
        WrappedPeriod period = WrappedPeriod.parse(periodKey);
        return summaryRepository.findByScopeAndUserIsNullAndPeriodTypeAndPeriodKey(WrappedScope.PLATFORM,
                        period.type(), period.key())
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Mutuals Wrapped " + periodKey + " is not published yet"));
    }

    @Transactional(readOnly = true)
    public AllTimeWrappedResponse allTime() {
        User me = currentUserService.getCurrentUser();
        boolean premium = me.hasRole(Role.PREMIUM);
        UserWrappedStats overall = calculator.forUser(me.getId(), WrappedPeriod.allTime(LocalDate.now(clock)), zone());
        List<FriendWrappedSection> sections = calculator.friendSections(me.getId());
        return new AllTimeWrappedResponse(overall, premium,
                premium ? sections : sections.stream().limit(FREE_FRIEND_SECTIONS).toList());
    }

    @Transactional(readOnly = true)
    public FriendWrappedSection allTimeWithFriend(Long friendId) {
        Long myId = currentUserService.getCurrentUserId();
        return calculator.friendSections(myId).stream()
                .filter(section -> section.friend().id().equals(friendId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("You have no history with user " + friendId));
    }

    private WrappedResponse toResponse(WrappedSummary summary) {
        try {
            return new WrappedResponse(summary.getId(), summary.getScope(), summary.getPeriodType(),
                    summary.getPeriodKey(), summary.getGeneratedAt(), objectMapper.readTree(summary.getStatsJson()));
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Stored wrapped stats are not valid JSON", ex);
        }
    }

    private String toJson(Object stats) {
        try {
            return objectMapper.writeValueAsString(stats);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Could not serialize wrapped stats", ex);
        }
    }

    private ZoneId zone() {
        return ZoneId.of(properties.zone());
    }
}
