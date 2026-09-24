package com.mutuals.streak.controller;

import com.mutuals.streak.dto.StreakDetailResponse;
import com.mutuals.streak.dto.StreakHistoryResponse;
import com.mutuals.streak.service.StreakService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Streaks", description = "Rachas diarias por pareja, escudos e historial")
@RestController
@RequestMapping("/api/v1/streaks")
@RequiredArgsConstructor
public class StreakController {

    private final StreakService streakService;

    @Operation(summary = "Historial de rachas agrupado por amigo (incluye ex mutuals)")
    @GetMapping("/history")
    public ResponseEntity<List<StreakHistoryResponse>> history(
            @RequestParam(defaultValue = "false") boolean includeBlocked) {
        return ResponseEntity.ok(streakService.history(includeBlocked));
    }

    @Operation(summary = "Detalle de una racha")
    @GetMapping("/{streakId}")
    public ResponseEntity<StreakDetailResponse> getDetail(@PathVariable Long streakId) {
        return ResponseEntity.ok(streakService.getDetail(streakId));
    }

    @Operation(summary = "Activar un escudo para proteger la racha un día sin interacción")
    @PostMapping("/{streakId}/shield")
    public ResponseEntity<StreakDetailResponse> armShield(@PathVariable Long streakId) {
        return ResponseEntity.ok(streakService.armShield(streakId));
    }
}
