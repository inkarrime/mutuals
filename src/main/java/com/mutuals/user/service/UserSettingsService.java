package com.mutuals.user.service;

import com.mutuals.achievement.repository.UserAchievementRepository;
import com.mutuals.common.exception.ForbiddenOperationException;
import com.mutuals.common.exception.InvalidOperationException;
import com.mutuals.security.CurrentUserService;
import com.mutuals.user.dto.CustomizationResponse;
import com.mutuals.user.dto.PreferencesResponse;
import com.mutuals.user.dto.UpdateCustomizationRequest;
import com.mutuals.user.dto.UpdatePreferencesRequest;
import com.mutuals.user.entity.ProfileCustomization;
import com.mutuals.user.entity.Role;
import com.mutuals.user.entity.User;
import com.mutuals.user.entity.UserPreferences;
import com.mutuals.user.mapper.UserMapper;
import com.mutuals.user.repository.UserLocationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserSettingsService {

    private static final String PLUS_REQUIRED = "This customization requires Mutuals Plus";

    private final CurrentUserService currentUserService;
    private final UserLocationRepository userLocationRepository;
    private final UserAchievementRepository userAchievementRepository;
    private final UserMapper userMapper;
    private final Clock clock;

    @Transactional(readOnly = true)
    public PreferencesResponse getPreferences() {
        return userMapper.toPreferences(currentUserService.getCurrentUser().getPreferences());
    }

    @Transactional
    public PreferencesResponse updatePreferences(UpdatePreferencesRequest request) {
        User user = currentUserService.getCurrentUser();
        UserPreferences preferences = user.getPreferences();
        if (request.locationSharingEnabled() != null) {
            applyLocationConsent(user, preferences, request.locationSharingEnabled(), request.locationConsentVersion());
        }
        if (request.onboardingCompleted() != null) {
            preferences.setOnboardingCompleted(request.onboardingCompleted());
        }
        if (request.language() != null) {
            preferences.setLanguage(request.language());
        }
        if (request.pushEnabled() != null) {
            preferences.setPushEnabled(request.pushEnabled());
        }
        if (request.emailNotificationsEnabled() != null) {
            preferences.setEmailNotificationsEnabled(request.emailNotificationsEnabled());
        }
        if (request.muteHighlights() != null) {
            preferences.setMuteHighlights(request.muteHighlights());
        }
        return userMapper.toPreferences(preferences);
    }

    @Transactional(readOnly = true)
    public CustomizationResponse getCustomization() {
        return userMapper.toCustomization(currentUserService.getCurrentUser().getCustomization());
    }

    @Transactional
    public CustomizationResponse updateCustomization(UpdateCustomizationRequest request) {
        User user = currentUserService.getCurrentUser();
        boolean premium = user.hasRole(Role.PREMIUM);
        ProfileCustomization customization = user.getCustomization();
        if (request.theme() != null) {
            requirePlusIf(request.theme().isPremium(), premium);
            customization.setTheme(request.theme());
        }
        if (request.fireStyle() != null) {
            requirePlusIf(request.fireStyle().isPremium(), premium);
            customization.setFireStyle(request.fireStyle());
        }
        if (request.appIcon() != null) {
            requirePlusIf(request.appIcon().isPremium(), premium);
            customization.setAppIcon(request.appIcon());
        }
        if (request.bannerUrl() != null) {
            requirePlusIf(true, premium);
            customization.setBannerUrl(request.bannerUrl());
        }
        if (request.widgetConfig() != null) {
            requirePlusIf(true, premium);
            customization.setWidgetConfig(request.widgetConfig());
        }
        if (request.pinnedAchievementCodes() != null) {
            customization.setPinnedAchievementCodes(validatePinned(user.getId(), request.pinnedAchievementCodes()));
        }
        return userMapper.toCustomization(customization);
    }

    private void applyLocationConsent(User user, UserPreferences preferences, boolean enabled, String consentVersion) {
        if (enabled) {
            if (consentVersion == null || consentVersion.isBlank()) {
                throw new InvalidOperationException("locationConsentVersion is required to enable location sharing");
            }
            preferences.setLocationSharingEnabled(true);
            preferences.setLocationConsentAt(clock.instant());
            preferences.setLocationConsentVersion(consentVersion);
        } else {
            preferences.setLocationSharingEnabled(false);
            userLocationRepository.deleteByUserId(user.getId());
        }
    }

    private List<String> validatePinned(Long userId, List<String> codes) {
        Set<String> owned = userAchievementRepository.findAllByUserWithAchievement(userId).stream()
                .map(userAchievement -> userAchievement.getAchievement().getCode())
                .collect(Collectors.toSet());
        if (!owned.containsAll(codes)) {
            throw new InvalidOperationException("You can only pin achievements you have unlocked");
        }
        return new ArrayList<>(codes.stream().distinct().toList());
    }

    private void requirePlusIf(boolean requiresPlus, boolean premium) {
        if (requiresPlus && !premium) {
            throw new ForbiddenOperationException(PLUS_REQUIRED);
        }
    }
}
