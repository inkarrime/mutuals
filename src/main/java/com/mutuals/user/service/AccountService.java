package com.mutuals.user.service;

import com.mutuals.auth.service.RefreshTokenService;
import com.mutuals.security.CurrentUserService;
import com.mutuals.social.entity.MutualEndReason;
import com.mutuals.social.entity.MutualStatus;
import com.mutuals.social.repository.FollowRepository;
import com.mutuals.social.repository.MutualRepository;
import com.mutuals.social.service.MutualService;
import com.mutuals.user.entity.User;
import com.mutuals.user.entity.UserStatus;
import com.mutuals.user.repository.DeviceRepository;
import com.mutuals.user.repository.UserLocationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AccountService {

    private static final String DELETED_NAME = "Usuario eliminado";

    private final CurrentUserService currentUserService;
    private final MutualRepository mutualRepository;
    private final MutualService mutualService;
    private final FollowRepository followRepository;
    private final DeviceRepository deviceRepository;
    private final UserLocationRepository locationRepository;
    private final RefreshTokenService refreshTokenService;

    @Transactional
    public void deleteMyAccount() {
        User user = currentUserService.getCurrentUser();
        Long userId = user.getId();
        mutualRepository.findByUserAndStatus(userId, MutualStatus.ACTIVE)
                .forEach(mutual -> mutualService.end(mutual, MutualEndReason.ACCOUNT_DELETED));
        followRepository.deleteAllInvolving(userId);
        deviceRepository.deleteAll(deviceRepository.findByUserId(userId));
        locationRepository.deleteByUserId(userId);
        refreshTokenService.revokeAll(userId);
        anonymize(user);
    }

    private void anonymize(User user) {
        user.setStatus(UserStatus.DELETED);
        user.setEmail("deleted-" + user.getId() + "@mutuals.invalid");
        user.setUsername("deleted_" + user.getId());
        user.setDisplayName(DELETED_NAME);
        user.setBio(null);
        user.setAvatarUrl(null);
        user.getPreferences().setLocationSharingEnabled(false);
    }
}
