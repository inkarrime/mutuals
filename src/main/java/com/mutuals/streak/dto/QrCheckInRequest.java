package com.mutuals.streak.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record QrCheckInRequest(@NotBlank @Size(max = 200) String token) {
}
