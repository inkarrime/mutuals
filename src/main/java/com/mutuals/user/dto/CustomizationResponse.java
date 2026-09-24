package com.mutuals.user.dto;

import com.mutuals.user.entity.AppIcon;
import com.mutuals.user.entity.FireStyle;
import com.mutuals.user.entity.ProfileTheme;

import java.util.List;

public record CustomizationResponse(
        ProfileTheme theme,
        FireStyle fireStyle,
        AppIcon appIcon,
        String bannerUrl,
        List<String> pinnedAchievementCodes,
        String widgetConfig
) {
}
