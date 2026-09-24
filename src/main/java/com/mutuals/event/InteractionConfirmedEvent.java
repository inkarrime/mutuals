package com.mutuals.event;

import com.mutuals.streak.entity.InteractionMethod;

public record InteractionConfirmedEvent(Long interactionId, Long streakId, InteractionMethod method, Long initiatorId, Long confirmerId, int streakLength, boolean countedToday) {
}
