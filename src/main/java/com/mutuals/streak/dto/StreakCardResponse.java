package com.mutuals.streak.dto;

import java.time.LocalDate;

public record StreakCardResponse(
        Long id,
        int currentLength,
        boolean countedToday,
        boolean atRisk,
        boolean shieldArmed,
        boolean pure,
        LocalDate startDate
) {
}
