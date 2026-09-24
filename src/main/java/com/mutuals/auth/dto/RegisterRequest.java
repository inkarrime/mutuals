package com.mutuals.auth.dto;

import com.mutuals.common.validation.StrongPassword;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank @Email @Size(max = 120) String email,
        @NotBlank
        @Pattern(regexp = "^[a-z0-9._]{3,30}$", message = "Username must be 3-30 lowercase letters, numbers, dots or underscores")
        String username,
        @NotBlank @Size(max = 50) String displayName,
        @StrongPassword String password
) {
}
