package com.mutuals.wrapped.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.mutuals.wrapped.entity.WrappedPeriodType;
import com.mutuals.wrapped.entity.WrappedScope;

import java.time.Instant;

public record WrappedResponse(
        Long id,
        WrappedScope scope,
        WrappedPeriodType periodType,
        String periodKey,
        Instant generatedAt,
        JsonNode stats
) {
}
