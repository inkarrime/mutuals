package com.mutuals.challenge.dto;

import com.mutuals.challenge.entity.ChallengeType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ChallengeTemplateRequest(
        @NotBlank @Size(max = 80) String title,
        @NotBlank @Size(max = 500) String prompt,
        @NotNull ChallengeType type,
        @NotNull @Min(0) @Max(100) Integer rewardGems,
        boolean requiresPresence,
        boolean requiresPhoto,
        boolean active
) {
}
