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
@Table(name = "activities", indexes = @Index(name = "idx_activities_actor_created", columnList = "actor_id, created_at"))
public class Activity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "actor_id", nullable = false)
    private User actor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ActivityType type;

    @NotBlank
    @Size(max = 140)
    @Column(nullable = false, length = 140)
    private String title;

    @Column(name = "metric_value")
    private Integer value;

    @Min(0)
    @Column(nullable = false)
    private int likeCount;

    @Min(0)
    @Column(nullable = false)
    private int commentCount;
}
