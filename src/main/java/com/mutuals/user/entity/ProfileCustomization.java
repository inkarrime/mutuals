package com.mutuals.user.entity;

import com.mutuals.common.entity.BaseEntity;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "profile_customizations")
public class ProfileCustomization extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProfileTheme theme = ProfileTheme.DEFAULT;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FireStyle fireStyle = FireStyle.CLASSIC;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AppIcon appIcon = AppIcon.DEFAULT;

    @Size(max = 500)
    @Column(length = 500)
    private String bannerUrl;

    @Size(max = 3)
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "profile_pinned_achievements", joinColumns = @JoinColumn(name = "customization_id"))
    @OrderColumn(name = "pin_order")
    @Column(name = "achievement_code", length = 50)
    private List<String> pinnedAchievementCodes = new ArrayList<>();

    @Size(max = 1000)
    @Column(length = 1000)
    private String widgetConfig;
}
