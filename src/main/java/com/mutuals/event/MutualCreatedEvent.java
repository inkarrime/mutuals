package com.mutuals.event;


public record MutualCreatedEvent(Long mutualId, Long userAId, Long userBId) {
}
