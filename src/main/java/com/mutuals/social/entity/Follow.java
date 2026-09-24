package com.mutuals.social.entity;

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
@Table(name = "follows",
        uniqueConstraints = @UniqueConstraint(name = "uk_follows_pair", columnNames = {"follower_id", "followed_id"}),
        indexes = {
                @Index(name = "idx_follows_follower", columnList = "follower_id"),
                @Index(name = "idx_follows_followed", columnList = "followed_id")
        })
public class Follow extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "follower_id", nullable = false)
    private User follower;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "followed_id", nullable = false)
    private User followed;

    public Follow(User follower, User followed) {
        this.follower = follower;
        this.followed = followed;
    }
}
