package com.mutuals.user.entity;

public enum AppIcon {
    DEFAULT(false),
    DARK(true),
    RETRO(true),
    GOLD(true);

    private final boolean premium;

    AppIcon(boolean premium) {
        this.premium = premium;
    }

    public boolean isPremium() {
        return premium;
    }
}
