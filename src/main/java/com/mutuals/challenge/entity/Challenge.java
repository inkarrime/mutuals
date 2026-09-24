package com.mutuals.challenge.entity;

import com.mutuals.common.entity.BaseEntity;
import com.mutuals.user.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.mutuals.social.entity.Mutual;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "challenges",
        uniqueConstraints = @UniqueConstraint(name = "uk_challenges_mutual_week", columnNames = {"mutual_id", "week_start"}),
        indexes = @Index(name = "idx_challenges_status", columnList = "status"))
public class Challenge extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "template_id", nullable = false)
    private ChallengeTemplate template;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "mutual_id", nullable = false)
    private Mutual mutual;

    @Column(name = "week_start", nullable = false)
    private LocalDate weekStart;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private ChallengeStatus status = ChallengeStatus.OPEN;

    private Instant completedAt;

    @OneToMany(mappedBy = "challenge", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChallengeSubmission> submissions = new ArrayList<>();

    public void addSubmission(ChallengeSubmission submission) {
        submission.setChallenge(this);
        submissions.add(submission);
    }

    public boolean hasSubmissionFrom(Long userId) {
        return submissions.stream().anyMatch(submission -> submission.getUser().getId().equals(userId));
    }
}
