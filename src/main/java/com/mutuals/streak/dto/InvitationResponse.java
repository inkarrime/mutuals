package com.mutuals.streak.dto;

import com.mutuals.streak.entity.InvitationStatus;
import com.mutuals.user.dto.UserSummaryResponse;

import java.time.Instant;

public record InvitationResponse(
        Long id,
        UserSummaryResponse inviter,
        UserSummaryResponse invitee,
        InvitationStatus status,
        Instant createdAt
) {
}
