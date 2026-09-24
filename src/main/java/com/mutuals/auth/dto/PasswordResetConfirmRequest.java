package com.mutuals.auth.dto;

import com.mutuals.common.validation.StrongPassword;
import jakarta.validation.constraints.NotBlank;

public record PasswordResetConfirmRequest(
        @NotBlank String token,
        @StrongPassword String newPassword
) {
}
