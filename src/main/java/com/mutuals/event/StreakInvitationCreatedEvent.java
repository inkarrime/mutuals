package com.mutuals.event;


public record StreakInvitationCreatedEvent(Long invitationId, Long inviterId, Long inviteeId) {
}
