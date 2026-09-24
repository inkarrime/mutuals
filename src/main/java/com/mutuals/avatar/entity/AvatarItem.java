package com.mutuals.avatar.entity;

import com.mutuals.common.entity.BaseEntity;
import com.mutuals.user.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.mutuals.achievement.entity.Achievement;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "avatar_items",
        uniqueConstraints = @UniqueConstraint(name = "uk_avatar_items_code", columnNames = "code"),
        indexes = @Index(name = "idx_avatar_items_layer", columnList = "layer, active"))
public class AvatarItem extends BaseEntity {

    @NotBlank
    @Pattern(regexp = "^[A-Z0-9_]{3,50}$")
    @Column(nullable = false, length = 50)
    private String code;

    @NotBlank
    @Size(max = 60)
    @Column(nullable = false, length = 60)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private AvatarLayer layer;

    @Min(0)
    @Column(nullable = false)
    private int priceGems;

    @Column(nullable = false)
    private boolean premiumOnly;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unlock_achievement_id")
    private Achievement unlockAchievement;

    @Size(max = 500)
    @Column(length = 500)
    private String imageUrl;

    @Column(nullable = false)
    private boolean active = true;
}
