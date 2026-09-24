package com.mutuals.streak.entity;

import com.mutuals.common.entity.BaseEntity;
import com.mutuals.user.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "interactions", indexes = {
        @Index(name = "idx_interactions_streak_status", columnList = "streak_id, status"),
        @Index(name = "idx_interactions_status_expires", columnList = "status, expires_at")
})
public class Interaction extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "streak_id", nullable = false)
    private Streak streak;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "initiator_id", nullable = false)
    private User initiator;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "confirmer_id")
    private User confirmer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private InteractionMethod method;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private InteractionStatus status = InteractionStatus.PENDING;

    @Size(max = 140)
    @Column(length = 140)
    private String note;

    @Column(name = "expires_at")
    private Instant expiresAt;

    private Instant confirmedAt;

    private LocalDate interactionDate;

    public boolean isPending() {
        return status == InteractionStatus.PENDING;
    }
}
