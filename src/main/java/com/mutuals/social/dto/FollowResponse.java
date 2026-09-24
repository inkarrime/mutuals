package com.mutuals.social.dto;

public record FollowResponse(Long userId, boolean following, boolean mutual) {
}
