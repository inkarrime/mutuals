package com.mutuals.avatar.dto;

import com.mutuals.avatar.entity.AvatarLayer;

public record AvatarItemResponse(
        Long id,
        String code,
        String name,
        AvatarLayer layer,
        int priceGems,
        boolean premiumOnly,
        String unlockAchievementCode,
        String imageUrl,
        boolean owned,
        boolean equipped
) {
}
