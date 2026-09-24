package com.mutuals.activity.entity;

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
@Table(name = "activity_comments", indexes = @Index(name = "idx_activity_comments_activity", columnList = "activity_id"))
public class ActivityComment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "activity_id", nullable = false)
    private Activity activity;

    @NotBlank
    @Size(max = 300)
    @Column(nullable = false, length = 300)
    private String content;

    @Column(nullable = false)
    private boolean hidden;
}
