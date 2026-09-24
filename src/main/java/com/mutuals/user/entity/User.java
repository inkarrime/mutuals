package com.mutuals.user.entity;

import com.mutuals.common.entity.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "users",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_users_email", columnNames = "email"),
                @UniqueConstraint(name = "uk_users_username", columnNames = "username")
        },
        indexes = @Index(name = "idx_users_status", columnList = "status"))
public class User extends BaseEntity {

    @NotBlank
    @Email
    @Size(max = 120)
    @Column(nullable = false, length = 120)
    private String email;

    @NotBlank
    @Pattern(regexp = "^[a-z0-9._]{3,30}$")
    @Column(nullable = false, length = 30)
    private String username;

    @NotBlank
    @Size(max = 50)
    @Column(nullable = false, length = 50)
    private String displayName;

    @NotBlank
    @Column(nullable = false)
    private String passwordHash;

    @Size(max = 160)
    @Column(length = 160)
    private String bio;

    @Size(max = 500)
    @Column(length = 500)
    private String avatarUrl;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private Set<Role> roles = new HashSet<>(EnumSet.of(Role.USER));

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserStatus status = UserStatus.ACTIVE;

    @Min(0)
    @Column(nullable = false)
    private int gems;

    @Min(0)
    @Column(nullable = false)
    private int shields;

    @Min(0)
    @Column(nullable = false)
    private int personalStreak;

    @Min(0)
    @Column(nullable = false)
    private int longestPersonalStreak;

    private LocalDate personalStreakLastDate;

    private Instant lastActiveAt;

    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private UserPreferences preferences;

    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private ProfileCustomization customization;

    public boolean hasRole(Role role) {
        return roles.contains(role);
    }

    public boolean isActive() {
        return status == UserStatus.ACTIVE;
    }

    public void attachPreferences(UserPreferences userPreferences) {
        userPreferences.setUser(this);
        this.preferences = userPreferences;
    }

    public void attachCustomization(ProfileCustomization profileCustomization) {
        profileCustomization.setUser(this);
        this.customization = profileCustomization;
    }
}
