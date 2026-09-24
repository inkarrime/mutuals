package com.mutuals.challenge.dto;

import com.mutuals.challenge.entity.ChallengeType;

public record ChallengeTemplateResponse(
        Long id,
        String title,
        String prompt,
        ChallengeType type,
        int rewardGems,
        boolean requiresPresence,
        boolean requiresPhoto,
        boolean active
) {
}
