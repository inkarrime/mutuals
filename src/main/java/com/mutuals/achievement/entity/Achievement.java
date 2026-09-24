package com.mutuals.achievement.entity;

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
@Table(name = "achievements",
        uniqueConstraints = @UniqueConstraint(name = "uk_achievements_code", columnNames = "code"))
public class Achievement extends BaseEntity {

    @NotBlank
    @Pattern(regexp = "^[A-Z0-9_]{3,50}$")
    @Column(nullable = false, length = 50)
    private String code;

    @NotBlank
    @Size(max = 60)
    @Column(nullable = false, length = 60)
    private String name;

    @Size(max = 200)
    @Column(length = 200)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AchievementType type;

    @Min(1)
    @Column(nullable = false)
    private int threshold;

    @Min(0)
    @Column(nullable = false)
    private int rewardGems;

    @Size(max = 500)
    @Column(length = 500)
    private String iconUrl;
}
