package com.mutuals.user.dto;

import com.mutuals.user.entity.AppIcon;
import com.mutuals.user.entity.FireStyle;
import com.mutuals.user.entity.ProfileTheme;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

public record UpdateCustomizationRequest(
        ProfileTheme theme,
        FireStyle fireStyle,
        AppIcon appIcon,
        @Size(max = 500) @Pattern(regexp = "^https://.*", message = "Banner URL must use https") String bannerUrl,
        @Size(max = 3) List<@Pattern(regexp = "^[A-Z0-9_]{3,50}$") String> pinnedAchievementCodes,
        @Size(max = 1000) String widgetConfig
) {
}
