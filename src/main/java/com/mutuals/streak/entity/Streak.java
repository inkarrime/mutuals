package com.mutuals.streak.entity;

import com.mutuals.common.entity.BaseEntity;
import com.mutuals.user.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.mutuals.social.entity.Mutual;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "streaks", indexes = {
        @Index(name = "idx_streaks_mutual_status", columnList = "mutual_id, status"),
        @Index(name = "idx_streaks_status_last_active", columnList = "status, last_active_date")
})
public class Streak extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "mutual_id", nullable = false)
    private Mutual mutual;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private StreakStatus status = StreakStatus.ACTIVE;

    @Min(0)
    @Column(nullable = false)
    private int currentLength;

    @Column(nullable = false)
    private LocalDate startDate;

    private LocalDate endDate;

    @Column(name = "last_active_date")
    private LocalDate lastActiveDate;

    @Min(0)
    @Column(nullable = false)
    private int shieldsUsed;

    @Column(nullable = false)
    private boolean shieldArmed;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shield_armed_by_id")
    private User shieldArmedBy;

    private LocalDate lastShieldUsedOn;

    @Min(0)
    @Column(nullable = false)
    private int weeksRewarded;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private StreakEndReason endReason;

    public boolean isActive() {
        return status == StreakStatus.ACTIVE;
    }

    public boolean isPure() {
        return shieldsUsed == 0;
    }

    public boolean isCountedOn(LocalDate date) {
        return date.equals(lastActiveDate);
    }
}
