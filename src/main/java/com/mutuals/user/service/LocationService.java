package com.mutuals.user.service;

import com.mutuals.user.entity.User;
import com.mutuals.user.entity.UserLocation;
import com.mutuals.user.repository.UserLocationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LocationService {

    private final UserLocationRepository locationRepository;
    private final Clock clock;

    @Transactional
    public UserLocation savePing(User user, double latitude, double longitude, Double accuracyMeters) {
        UserLocation location = locationRepository.findByUserId(user.getId()).orElseGet(UserLocation::new);
        location.setUser(user);
        location.setLatitude(latitude);
        location.setLongitude(longitude);
        location.setAccuracyMeters(accuracyMeters);
        location.setRecordedAt(clock.instant());
        return locationRepository.save(location);
    }

    @Transactional(readOnly = true)
    public Optional<UserLocation> findRecent(Long userId, Instant since) {
        return locationRepository.findByUserId(userId).filter(location -> location.getRecordedAt().isAfter(since));
    }

    @Transactional(readOnly = true)
    public List<UserLocation> findRecent(Collection<Long> userIds, Instant since) {
        if (userIds.isEmpty()) {
            return List.of();
        }
        return locationRepository.findByUserIdInAndRecordedAtAfter(userIds, since);
    }

    @Transactional
    public int purgeOlderThan(Instant before) {
        return locationRepository.deleteOlderThan(before);
    }
}
