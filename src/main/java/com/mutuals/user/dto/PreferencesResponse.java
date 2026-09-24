package com.mutuals.user.dto;

import com.mutuals.user.entity.Language;

import java.time.Instant;

public record PreferencesResponse(
        boolean locationSharingEnabled,
        Instant locationConsentAt,
        String locationConsentVersion,
        boolean onboardingCompleted,
        Language language,
        boolean pushEnabled,
        boolean emailNotificationsEnabled,
        boolean muteHighlights
) {
}
