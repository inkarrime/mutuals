package com.mutuals.user.mapper;

import com.mutuals.user.dto.CustomizationResponse;
import com.mutuals.user.dto.DeviceResponse;
import com.mutuals.user.dto.MeResponse;
import com.mutuals.user.dto.PreferencesResponse;
import com.mutuals.user.dto.UserSummaryResponse;
import com.mutuals.user.entity.Device;
import com.mutuals.user.entity.ProfileCustomization;
import com.mutuals.user.entity.Role;
import com.mutuals.user.entity.User;
import com.mutuals.user.entity.UserPreferences;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
public class UserMapper {

    public UserSummaryResponse toSummary(User user) {
        return new UserSummaryResponse(user.getId(), user.getUsername(), user.getDisplayName(), user.getAvatarUrl(),
                user.getPersonalStreak());
    }

    public MeResponse toMe(User user) {
        UserPreferences preferences = user.getPreferences();
        return new MeResponse(
                user.getId(),
                user.getEmail(),
                user.getUsername(),
                user.getDisplayName(),
                user.getBio(),
                user.getAvatarUrl(),
                Set.copyOf(user.getRoles()),
                user.getGems(),
                user.getShields(),
                user.getPersonalStreak(),
                user.getLongestPersonalStreak(),
                user.hasRole(Role.PREMIUM),
                preferences != null && preferences.isOnboardingCompleted());
    }

    public PreferencesResponse toPreferences(UserPreferences preferences) {
        return new PreferencesResponse(
                preferences.isLocationSharingEnabled(),
                preferences.getLocationConsentAt(),
                preferences.getLocationConsentVersion(),
                preferences.isOnboardingCompleted(),
                preferences.getLanguage(),
                preferences.isPushEnabled(),
                preferences.isEmailNotificationsEnabled(),
                preferences.isMuteHighlights());
    }

    public CustomizationResponse toCustomization(ProfileCustomization customization) {
        if (customization == null) {
            return null;
        }
        return new CustomizationResponse(
                customization.getTheme(),
                customization.getFireStyle(),
                customization.getAppIcon(),
                customization.getBannerUrl(),
                List.copyOf(customization.getPinnedAchievementCodes()),
                customization.getWidgetConfig());
    }

    public DeviceResponse toDevice(Device device) {
        return new DeviceResponse(device.getId(), device.getPlatform(), device.getLastSeenAt());
    }
}
