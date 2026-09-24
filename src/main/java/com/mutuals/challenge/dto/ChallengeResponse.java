package com.mutuals.challenge.dto;

import com.mutuals.challenge.entity.ChallengeStatus;
import com.mutuals.user.dto.UserSummaryResponse;

import java.time.LocalDate;
import java.util.List;

public record ChallengeResponse(
        Long id,
        ChallengeTemplateResponse template,
        UserSummaryResponse friend,
        LocalDate weekStart,
        ChallengeStatus status,
        boolean submittedByMe,
        List<SubmissionResponse> submissions
) {
}
