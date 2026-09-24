package com.mutuals.streak.dto;

public record ProximityCheckInResponse(
        ProximityCheckInStatus status,
        String message,
        InteractionResponse interaction
) {
    public enum ProximityCheckInStatus {
        WAITING_FOR_FRIEND,
        CONFIRMED
    }
}
