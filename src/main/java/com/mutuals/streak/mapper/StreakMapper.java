package com.mutuals.streak.mapper;

import com.mutuals.streak.dto.InteractionResponse;
import com.mutuals.streak.dto.InvitationResponse;
import com.mutuals.streak.dto.StreakCardResponse;
import com.mutuals.streak.dto.StreakDetailResponse;
import com.mutuals.streak.entity.Interaction;
import com.mutuals.streak.entity.Streak;
import com.mutuals.streak.entity.StreakInvitation;
import com.mutuals.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class StreakMapper {

    private final UserMapper userMapper;

    public StreakCardResponse toCard(Streak streak, LocalDate today) {
        boolean countedToday = streak.isCountedOn(today);
        boolean atRisk = streak.isActive() && streak.getCurrentLength() > 0 && !countedToday;
        return new StreakCardResponse(streak.getId(), streak.getCurrentLength(), countedToday, atRisk,
                streak.isShieldArmed(), streak.isPure(), streak.getStartDate());
    }

    public StreakDetailResponse toDetail(Streak streak, Long viewerId, LocalDate today) {
        return new StreakDetailResponse(
                streak.getId(),
                userMapper.toSummary(streak.getMutual().other(viewerId)),
                streak.getStatus(),
                streak.getCurrentLength(),
                streak.getStartDate(),
                streak.getEndDate(),
                streak.getLastActiveDate(),
                streak.isCountedOn(today),
                streak.isShieldArmed(),
                streak.getShieldsUsed(),
                streak.isPure(),
                streak.getEndReason());
    }

    public InvitationResponse toInvitation(StreakInvitation invitation) {
        return new InvitationResponse(
                invitation.getId(),
                userMapper.toSummary(invitation.getInviter()),
                userMapper.toSummary(invitation.getInvitee()),
                invitation.getStatus(),
                invitation.getCreatedAt());
    }

    public InteractionResponse toInteraction(Interaction interaction) {
        return new InteractionResponse(
                interaction.getId(),
                interaction.getStreak().getId(),
                userMapper.toSummary(interaction.getInitiator()),
                interaction.getConfirmer() == null ? null : userMapper.toSummary(interaction.getConfirmer()),
                interaction.getMethod(),
                interaction.getStatus(),
                interaction.getNote(),
                interaction.getExpiresAt(),
                interaction.getConfirmedAt(),
                interaction.getInteractionDate(),
                interaction.getStreak().getCurrentLength());
    }
}
