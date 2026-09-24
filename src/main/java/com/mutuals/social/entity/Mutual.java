package com.mutuals.social.entity;

import com.mutuals.common.entity.BaseEntity;
import com.mutuals.user.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "mutuals",
        uniqueConstraints = @UniqueConstraint(name = "uk_mutuals_pair", columnNames = {"user_a_id", "user_b_id"}),
        indexes = {
                @Index(name = "idx_mutuals_user_a", columnList = "user_a_id, status"),
                @Index(name = "idx_mutuals_user_b", columnList = "user_b_id, status")
        })
public class Mutual extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_a_id", nullable = false)
    private User userA;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_b_id", nullable = false)
    private User userB;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private MutualStatus status = MutualStatus.ACTIVE;

    @Column(nullable = false)
    private Instant firstMutualAt;

    @Column(nullable = false)
    private Instant currentSince;

    private Instant endedAt;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private MutualEndReason endReason;

    public boolean involves(Long userId) {
        return userA.getId().equals(userId) || userB.getId().equals(userId);
    }

    public User other(Long userId) {
        return userA.getId().equals(userId) ? userB : userA;
    }

    public boolean isActive() {
        return status == MutualStatus.ACTIVE;
    }
}
