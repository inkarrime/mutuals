package com.mutuals.avatar.entity;

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
@Table(name = "user_avatar_items",
        uniqueConstraints = @UniqueConstraint(name = "uk_user_avatar_items", columnNames = {"user_id", "item_id"}))
public class UserAvatarItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "item_id", nullable = false)
    private AvatarItem item;

    @Column(nullable = false)
    private boolean equipped;
}
