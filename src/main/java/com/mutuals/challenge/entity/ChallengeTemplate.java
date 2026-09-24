package com.mutuals.challenge.entity;

import com.mutuals.common.entity.BaseEntity;
import com.mutuals.user.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "challenge_templates", indexes = @Index(name = "idx_challenge_templates_active", columnList = "active"))
public class ChallengeTemplate extends BaseEntity {

    @NotBlank
    @Size(max = 80)
    @Column(nullable = false, length = 80)
    private String title;

    @NotBlank
    @Size(max = 500)
    @Column(nullable = false, length = 500)
    private String prompt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private ChallengeType type;

    @Min(0)
    @Max(100)
    @Column(nullable = false)
    private int rewardGems;

    @Column(nullable = false)
    private boolean requiresPresence;

    @Column(nullable = false)
    private boolean requiresPhoto;

    @Column(nullable = false)
    private boolean active = true;
}
