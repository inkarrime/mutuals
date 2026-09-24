package com.mutuals.challenge.dto;

import com.mutuals.user.dto.UserSummaryResponse;

import java.time.Instant;

public record SubmissionResponse(
        UserSummaryResponse user,
        boolean revealed,
        String content,
        String photoUrl,
        Instant submittedAt
) {
}
