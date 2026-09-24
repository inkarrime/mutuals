package com.mutuals.user.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
        @Size(min = 1, max = 50) String displayName,
        @Size(max = 160) String bio,
        @Size(max = 500) @Pattern(regexp = "^https://.*", message = "Avatar URL must use https") String avatarUrl
) {
}
