package com.mutuals.user.dto;

import com.mutuals.user.entity.Language;
import jakarta.validation.constraints.Size;

public record UpdatePreferencesRequest(
        Boolean locationSharingEnabled,
        @Size(max = 20) String locationConsentVersion,
        Boolean onboardingCompleted,
        Language language,
        Boolean pushEnabled,
        Boolean emailNotificationsEnabled,
        Boolean muteHighlights
) {
}
