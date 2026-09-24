package com.mutuals.challenge.entity;

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
@Table(name = "challenge_submissions",
        uniqueConstraints = @UniqueConstraint(name = "uk_challenge_submissions_user", columnNames = {"challenge_id", "user_id"}))
public class ChallengeSubmission extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "challenge_id", nullable = false)
    private Challenge challenge;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Size(max = 1000)
    @Column(length = 1000)
    private String content;

    @Size(max = 500)
    @Column(length = 500)
    private String photoUrl;

    @Column(nullable = false)
    private Instant submittedAt;
}
