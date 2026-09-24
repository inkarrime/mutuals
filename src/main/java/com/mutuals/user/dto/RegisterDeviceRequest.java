package com.mutuals.user.dto;

import com.mutuals.user.entity.DevicePlatform;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegisterDeviceRequest(
        @NotBlank @Size(max = 500) String fcmToken,
        @NotNull DevicePlatform platform
) {
}
