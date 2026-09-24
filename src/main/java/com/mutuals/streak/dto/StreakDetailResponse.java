package com.mutuals.streak.dto;

import com.mutuals.streak.entity.StreakEndReason;
import com.mutuals.streak.entity.StreakStatus;
import com.mutuals.user.dto.UserSummaryResponse;

import java.time.LocalDate;

public record StreakDetailResponse(
        Long id,
        UserSummaryResponse friend,
        StreakStatus status,
        int currentLength,
        LocalDate startDate,
        LocalDate endDate,
        LocalDate lastActiveDate,
        boolean countedToday,
        boolean shieldArmed,
        int shieldsUsed,
        boolean pure,
        StreakEndReason endReason
) {
}
