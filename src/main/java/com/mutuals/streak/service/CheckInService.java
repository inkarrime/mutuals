package com.mutuals.streak.service;

import com.mutuals.common.exception.InvalidCheckInException;
import com.mutuals.common.exception.InvalidQrTokenException;
import com.mutuals.common.util.GeoUtils;
import com.mutuals.config.AppProperties;
import com.mutuals.security.CurrentUserService;
import com.mutuals.streak.dto.InteractionResponse;
import com.mutuals.streak.dto.ProximityCheckInRequest;
import com.mutuals.streak.dto.ProximityCheckInResponse;
import com.mutuals.streak.dto.ProximityCheckInResponse.ProximityCheckInStatus;
import com.mutuals.streak.dto.QrCodeResponse;
import com.mutuals.streak.entity.Interaction;
import com.mutuals.streak.entity.InteractionMethod;
import com.mutuals.streak.entity.Streak;
import com.mutuals.streak.mapper.StreakMapper;
import com.mutuals.user.entity.User;
import com.mutuals.user.entity.UserLocation;
import com.mutuals.user.service.LocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CheckInService {

    private final QrTokenService qrTokenService;
    private final StreakService streakService;
    private final InteractionService interactionService;
    private final LocationService locationService;
    private final CurrentUserService currentUserService;
    private final StreakMapper streakMapper;
    private final AppProperties properties;
    private final Clock clock;

    public QrCodeResponse myQrCode() {
        return qrTokenService.generate(currentUserService.getCurrentUserId());
    }

    @Transactional
    public InteractionResponse checkInWithQr(String token) {
        User me = currentUserService.getCurrentUser();
        Long ownerId = qrTokenService.verify(token).orElseThrow(InvalidQrTokenException::new);
        if (ownerId.equals(me.getId())) {
            throw new InvalidCheckInException("You cannot scan your own code");
        }
        Streak streak = streakService.requireActiveStreakWith(me.getId(), ownerId);
        User owner = streak.getMutual().other(me.getId());
        Interaction interaction = interactionService.recordVerified(streak, me, owner, InteractionMethod.QR);
        return streakMapper.toInteraction(interaction);
    }

    @Transactional
    public ProximityCheckInResponse checkInByProximity(ProximityCheckInRequest request) {
        User me = currentUserService.getCurrentUser();
        Streak streak = streakService.requireActiveStreakWith(me.getId(), request.friendId());
        locationService.savePing(me, request.latitude(), request.longitude(), null);
        Instant freshSince = clock.instant().minus(Duration.ofSeconds(properties.proximity().maxTimeDiffSeconds()));
        Optional<UserLocation> friendPing = locationService.findRecent(request.friendId(), freshSince);
        if (friendPing.isEmpty()) {
            return new ProximityCheckInResponse(ProximityCheckInStatus.WAITING_FOR_FRIEND,
                    "Waiting for your friend to check in from their phone", null);
        }
        double distance = GeoUtils.distanceMeters(request.latitude(), request.longitude(),
                friendPing.get().getLatitude(), friendPing.get().getLongitude());
        if (distance > properties.proximity().maxDistanceMeters()) {
            throw new InvalidCheckInException("You are %d m apart; get within %d m to check in together"
                    .formatted(Math.round(distance), Math.round(properties.proximity().maxDistanceMeters())));
        }
        User friend = streak.getMutual().other(me.getId());
        Interaction interaction = interactionService.recordVerified(streak, friend, me, InteractionMethod.PROXIMITY);
        return new ProximityCheckInResponse(ProximityCheckInStatus.CONFIRMED, "Check-in confirmed",
                streakMapper.toInteraction(interaction));
    }
}
