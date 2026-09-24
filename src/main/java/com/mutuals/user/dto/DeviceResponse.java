package com.mutuals.user.dto;

import com.mutuals.user.entity.DevicePlatform;

import java.time.Instant;

public record DeviceResponse(Long id, DevicePlatform platform, Instant lastSeenAt) {
}
