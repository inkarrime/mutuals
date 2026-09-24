package com.mutuals.activity.dto;

import com.mutuals.activity.entity.ActivityType;
import com.mutuals.user.dto.UserSummaryResponse;

import java.time.Instant;

public record ActivityResponse(
        Long id,
        UserSummaryResponse actor,
        ActivityType type,
        String title,
        Integer value,
        int likeCount,
        int commentCount,
        boolean likedByMe,
        boolean fromMutual,
        boolean canComment,
        Instant createdAt
) {
}
