package com.mutuals.event;


public record UserFollowedEvent(Long followerId, Long followedId) {
}
