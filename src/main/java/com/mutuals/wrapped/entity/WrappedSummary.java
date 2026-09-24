package com.mutuals.wrapped.entity;

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
@Table(name = "wrapped_summaries",
        uniqueConstraints = @UniqueConstraint(name = "uk_wrapped_period",
                columnNames = {"scope", "user_id", "period_type", "period_key"}))
public class WrappedSummary extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private WrappedScope scope;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "period_type", nullable = false, length = 10)
    private WrappedPeriodType periodType;

    @NotBlank
    @Size(max = 10)
    @Column(name = "period_key", nullable = false, length = 10)
    private String periodKey;

    @NotBlank
    @Column(nullable = false, columnDefinition = "TEXT")
    private String statsJson;

    @Column(nullable = false)
    private Instant generatedAt;
}
