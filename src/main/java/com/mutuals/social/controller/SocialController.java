package com.mutuals.social.controller;

import com.mutuals.common.dto.PageResponse;
import com.mutuals.social.dto.CreateReportRequest;
import com.mutuals.social.dto.FollowResponse;
import com.mutuals.social.dto.MutualCardResponse;
import com.mutuals.social.dto.ReportResponse;
import com.mutuals.social.service.BlockService;
import com.mutuals.social.service.FollowService;
import com.mutuals.social.service.MutualService;
import com.mutuals.social.service.ReportService;
import com.mutuals.user.dto.UserSummaryResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Social", description = "Seguir, mutuals (follow mutuo), bloqueos y reportes")
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class SocialController {

    private final FollowService followService;
    private final MutualService mutualService;
    private final BlockService blockService;
    private final ReportService reportService;

    @Operation(summary = "Seguir a un usuario; si te sigue de vuelta se vuelven mutuals")
    @PostMapping("/users/{userId}/follow")
    public ResponseEntity<FollowResponse> follow(@PathVariable Long userId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(followService.follow(userId));
    }

    @Operation(summary = "Dejar de seguir. 409 si hay racha activa, salvo confirmStreakLoss=true")
    @DeleteMapping("/users/{userId}/follow")
    public ResponseEntity<Void> unfollow(@PathVariable Long userId,
                                         @RequestParam(defaultValue = "false") boolean confirmStreakLoss) {
        followService.unfollow(userId, confirmStreakLoss);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Grid del Home: mis mutuals con su racha (primero las que vencen hoy)")
    @GetMapping("/mutuals")
    public ResponseEntity<List<MutualCardResponse>> myMutuals() {
        return ResponseEntity.ok(mutualService.listMyMutuals());
    }

    @Operation(summary = "Bloquear a un usuario")
    @PostMapping("/users/{userId}/block")
    public ResponseEntity<Void> block(@PathVariable Long userId) {
        blockService.block(userId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(summary = "Desbloquear a un usuario")
    @DeleteMapping("/users/{userId}/block")
    public ResponseEntity<Void> unblock(@PathVariable Long userId) {
        blockService.unblock(userId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Usuarios que bloqueé")
    @GetMapping("/blocks")
    public ResponseEntity<PageResponse<UserSummaryResponse>> blocked(@PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(blockService.listBlocked(pageable));
    }

    @Operation(summary = "Reportar a un usuario")
    @PostMapping("/reports")
    public ResponseEntity<ReportResponse> report(@Valid @RequestBody CreateReportRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(reportService.create(request));
    }
}
