package com.mutuals.social.dto;

import com.mutuals.social.entity.ReportStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ReviewReportRequest(
        @NotNull ReportStatus status,
        @Size(max = 500) String resolutionNote,
        boolean suspendUser
) {
}
