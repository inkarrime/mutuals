package com.mutuals.streak.dto;

import com.mutuals.user.dto.UserSummaryResponse;

import java.util.List;

public record StreakHistoryResponse(
        UserSummaryResponse friend,
        boolean currentlyMutual,
        int totalStreaks,
        int bestLength,
        List<StreakDetailResponse> streaks
) {
}
