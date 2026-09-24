package com.mutuals.social.dto;

import com.mutuals.social.entity.ReportReason;
import com.mutuals.social.entity.ReportStatus;
import com.mutuals.user.dto.UserSummaryResponse;

import java.time.Instant;

public record AdminReportResponse(
        Long id,
        UserSummaryResponse reporter,
        UserSummaryResponse reported,
        ReportReason reason,
        String description,
        ReportStatus status,
        String resolutionNote,
        Instant createdAt,
        Instant reviewedAt
) {
}
