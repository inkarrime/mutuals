package com.mutuals.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "mutuals")
public record AppProperties(
        String zone,
        Jwt jwt,
        Cors cors,
        Streak streak,
        Proximity proximity,
        Qr qr,
        Shop shop,
        Mail mail,
        Push push,
        Storage storage,
        Seed seed
) {
    public record Jwt(String secret, long accessTokenMinutes, long refreshTokenDays) {
    }

    public record Cors(List<String> allowedOrigins) {
    }

    public record Streak(int maxActiveStreaksFree, int manualConfirmationHours, int gemsPerCompletedWeek,
                         List<Integer> milestones) {
    }

    public record Proximity(double maxDistanceMeters, long maxTimeDiffSeconds, long locationTtlMinutes,
                            double detectionDistanceMeters) {
    }

    public record Qr(String secret, long tokenSeconds) {
    }

    public record Shop(int shieldPriceGems) {
    }

    public record Mail(String from, String frontendUrl, boolean enabled) {
    }

    public record Push(String provider, String firebaseCredentialsPath) {
    }

    public record Storage(String provider, String localDir, String s3Bucket, String s3Region) {
    }

    public record Seed(String adminEmail, String adminPassword) {
    }
}
