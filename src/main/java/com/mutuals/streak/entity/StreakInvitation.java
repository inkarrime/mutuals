package com.mutuals.streak.entity;

import com.mutuals.common.entity.BaseEntity;
import com.mutuals.user.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.mutuals.social.entity.Mutual;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "streak_invitations", indexes = {
        @Index(name = "idx_streak_invitations_invitee", columnList = "invitee_id, status"),
        @Index(name = "idx_streak_invitations_mutual", columnList = "mutual_id, status")
})
public class StreakInvitation extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "mutual_id", nullable = false)
    private Mutual mutual;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "inviter_id", nullable = false)
    private User inviter;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "invitee_id", nullable = false)
    private User invitee;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private InvitationStatus status = InvitationStatus.PENDING;

    private Instant respondedAt;
}
