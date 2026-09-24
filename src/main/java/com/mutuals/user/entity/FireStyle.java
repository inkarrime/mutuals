package com.mutuals.user.entity;

public enum FireStyle {
    CLASSIC(false),
    BLUE(true),
    PURPLE(true),
    GOLD(true),
    RAINBOW(true);

    private final boolean premium;

    FireStyle(boolean premium) {
        this.premium = premium;
    }

    public boolean isPremium() {
        return premium;
    }
}
