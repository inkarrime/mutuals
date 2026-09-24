package com.mutuals.user.entity;

public enum ProfileTheme {
    DEFAULT(false),
    SUNSET(true),
    OCEAN(true),
    FOREST(true),
    NEON(true),
    MIDNIGHT(true);

    private final boolean premium;

    ProfileTheme(boolean premium) {
        this.premium = premium;
    }

    public boolean isPremium() {
        return premium;
    }
}
