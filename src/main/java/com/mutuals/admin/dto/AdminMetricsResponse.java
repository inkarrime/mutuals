package com.mutuals.admin.dto;

public record AdminMetricsResponse(
        long activeUsers,
        long suspendedUsers,
        long activeMutuals,
        long activeStreaks,
        long brokenStreaks,
        long pendingReports,
        long plusSubscribers
) {
}
