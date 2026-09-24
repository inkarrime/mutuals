package com.mutuals.streak.service;

import com.mutuals.common.exception.ForbiddenOperationException;
import com.mutuals.common.util.GeoUtils;
import com.mutuals.config.AppProperties;
import com.mutuals.event.ProximityDetectedEvent;
import com.mutuals.security.CurrentUserService;
import com.mutuals.social.entity.Mutual;
import com.mutuals.social.entity.MutualStatus;
import com.mutuals.social.repository.MutualRepository;
import com.mutuals.streak.entity.StreakStatus;
import com.mutuals.streak.repository.StreakRepository;
import com.mutuals.user.dto.LocationUpdateRequest;
import com.mutuals.user.entity.User;
import com.mutuals.user.entity.UserLocation;
import com.mutuals.user.service.LocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProximityDetectionService {

    private final LocationService locationService;
    private final MutualRepository mutualRepository;
    private final StreakRepository streakRepository;
    private final CurrentUserService currentUserService;
    private final AppProperties properties;
    private final ApplicationEventPublisher eventPublisher;
    private final Clock clock;

    @Transactional
    public int updateLocation(LocationUpdateRequest request) {
        User me = currentUserService.getCurrentUser();
        if (!me.getPreferences().isLocationSharingEnabled()) {
            throw new ForbiddenOperationException("Enable location sharing in your preferences first");
        }
        UserLocation mine = locationService.savePing(me, request.latitude(), request.longitude(), request.accuracyMeters());
        return detectNearbyMutuals(me, mine);
    }

    private int detectNearbyMutuals(User me, UserLocation mine) {
        List<Mutual> mutuals = mutualRepository.findByUserAndStatus(me.getId(), MutualStatus.ACTIVE);
        Map<Long, Mutual> byFriendId = mutuals.stream()
                .collect(Collectors.toMap(mutual -> mutual.other(me.getId()).getId(), Function.identity()));
        Instant since = clock.instant().minus(Duration.ofMinutes(properties.proximity().locationTtlMinutes()));
        LocalDate today = LocalDate.now(clock);
        int detected = 0;
        for (UserLocation friendLocation : locationService.findRecent(byFriendId.keySet(), since)) {
            User friend = friendLocation.getUser();
            if (!friend.getPreferences().isLocationSharingEnabled()) {
                continue;
            }
            double distance = GeoUtils.distanceMeters(mine.getLatitude(), mine.getLongitude(),
                    friendLocation.getLatitude(), friendLocation.getLongitude());
            if (distance > properties.proximity().detectionDistanceMeters()) {
                continue;
            }
            Mutual mutual = byFriendId.get(friend.getId());
            boolean published = streakRepository.findFirstByMutualIdAndStatus(mutual.getId(), StreakStatus.ACTIVE)
                    .filter(streak -> !streak.isCountedOn(today))
                    .map(streak -> {
                        eventPublisher.publishEvent(new ProximityDetectedEvent(streak.getId(), me.getId(), friend.getId()));
                        return true;
                    })
                    .orElse(false);
            if (published) {
                detected++;
            }
        }
        return detected;
    }
}
