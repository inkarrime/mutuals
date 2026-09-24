package com.mutuals.activity.dto;

import com.mutuals.user.dto.UserSummaryResponse;

import java.time.Instant;

public record CommentResponse(Long id, UserSummaryResponse author, String content, Instant createdAt) {
}
