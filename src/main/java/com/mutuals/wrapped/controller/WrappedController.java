package com.mutuals.wrapped.controller;

import com.mutuals.wrapped.dto.AllTimeWrappedResponse;
import com.mutuals.wrapped.dto.FriendWrappedSection;
import com.mutuals.wrapped.dto.WrappedResponse;
import com.mutuals.wrapped.service.WrappedService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Wrapped", description = "Resumen mensual, anual, histórico por amigo y el Wrapped de Mutuals")
@Validated
@RestController
@RequestMapping("/api/v1/wrapped")
@RequiredArgsConstructor
public class WrappedController {

    private static final String PERIOD_PATTERN = "^\\d{4}(-\\d{2})?$";

    private final WrappedService wrappedService;

    @Operation(summary = "Mis Wrapped generados")
    @GetMapping
    public ResponseEntity<List<WrappedResponse>> mine() {
        return ResponseEntity.ok(wrappedService.myWrappeds());
    }

    @Operation(summary = "Wrapped histórico (Plus ve todas las secciones por amigo)")
    @GetMapping("/all-time")
    public ResponseEntity<AllTimeWrappedResponse> allTime() {
        return ResponseEntity.ok(wrappedService.allTime());
    }

    @Operation(summary = "Wrapped histórico con un amigo específico (solo Plus)")
    @PreAuthorize("hasRole('PREMIUM')")
    @GetMapping("/all-time/mutuals/{friendId}")
    public ResponseEntity<FriendWrappedSection> allTimeWithFriend(@PathVariable Long friendId) {
        return ResponseEntity.ok(wrappedService.allTimeWithFriend(friendId));
    }

    @Operation(summary = "Wrapped público de Mutuals (datos agregados y anónimos)")
    @GetMapping("/platform/{periodKey}")
    public ResponseEntity<WrappedResponse> platform(@PathVariable @Pattern(regexp = PERIOD_PATTERN) String periodKey) {
        return ResponseEntity.ok(wrappedService.platformWrapped(periodKey));
    }

    @Operation(summary = "Mi Wrapped de un periodo (YYYY-MM o YYYY)")
    @GetMapping("/{periodKey}")
    public ResponseEntity<WrappedResponse> period(@PathVariable @Pattern(regexp = PERIOD_PATTERN) String periodKey) {
        return ResponseEntity.ok(wrappedService.myWrapped(periodKey));
    }
}
