package com.mutuals.social.dto;

import com.mutuals.social.entity.ReportReason;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CreateReportRequest(
        @NotNull @Positive Long reportedUserId,
        @NotNull ReportReason reason,
        @Size(max = 500) String description
) {
}
