package com.mutuals.common.exception;

import java.util.Map;

public class ActiveStreakConflictException extends ConflictException {

    public ActiveStreakConflictException(Long streakId, int currentLength) {
        super("You have an active streak with this user. Confirm with confirmStreakLoss=true to break it.",
                Map.of("streakId", streakId, "currentLength", currentLength));
    }
}
