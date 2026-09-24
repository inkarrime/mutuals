package com.mutuals.event;


public record ActivityLikedEvent(Long activityId, Long likerId) {
}
