package com.mutuals.user.entity;

import com.mutuals.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "user_locations", indexes = @Index(name = "idx_user_locations_recorded", columnList = "recorded_at"))
public class UserLocation extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @DecimalMin("-90.0")
    @DecimalMax("90.0")
    @Column(nullable = false)
    private double latitude;

    @DecimalMin("-180.0")
    @DecimalMax("180.0")
    @Column(nullable = false)
    private double longitude;

    private Double accuracyMeters;

    @Column(nullable = false)
    private Instant recordedAt;
}
