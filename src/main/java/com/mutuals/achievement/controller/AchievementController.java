package com.mutuals.achievement.controller;

import com.mutuals.achievement.dto.AchievementResponse;
import com.mutuals.achievement.service.AchievementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Achievements", description = "Logros y medallas")
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class AchievementController {

    private final AchievementService achievementService;

    @Operation(summary = "Catálogo de logros con mi progreso")
    @GetMapping("/achievements")
    public ResponseEntity<List<AchievementResponse>> catalog() {
        return ResponseEntity.ok(achievementService.catalogForMe());
    }

    @Operation(summary = "Logros desbloqueados por un usuario (perfil público)")
    @GetMapping("/users/{userId}/achievements")
    public ResponseEntity<List<AchievementResponse>> unlockedBy(@PathVariable Long userId) {
        return ResponseEntity.ok(achievementService.unlockedBy(userId));
    }
}
