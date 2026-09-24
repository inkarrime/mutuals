package com.mutuals.social.dto;

import com.mutuals.social.entity.ReportReason;
import com.mutuals.social.entity.ReportStatus;
import com.mutuals.user.dto.UserSummaryResponse;

import java.time.Instant;

public record ReportResponse(
        Long id,
        UserSummaryResponse reported,
        ReportReason reason,
        String description,
        ReportStatus status,
        Instant createdAt
) {
}
