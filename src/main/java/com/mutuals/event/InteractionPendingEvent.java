package com.mutuals.event;


public record InteractionPendingEvent(Long interactionId, Long initiatorId, Long recipientId) {
}
