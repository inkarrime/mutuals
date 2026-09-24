package com.mutuals.streak.controller;

import com.mutuals.common.dto.PageResponse;
import com.mutuals.streak.dto.CreateInteractionRequest;
import com.mutuals.streak.dto.InteractionResponse;
import com.mutuals.streak.service.InteractionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Interactions", description = "Flujo manual: uno marca, el otro confirma")
@RestController
@RequestMapping("/api/v1/interactions")
@RequiredArgsConstructor
public class InteractionController {

    private final InteractionService interactionService;

    @Operation(summary = "Marcar una interacción con un mutual (queda pendiente 24 h)")
    @PostMapping
    public ResponseEntity<InteractionResponse> create(@Valid @RequestBody CreateInteractionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(interactionService.createManual(request));
    }

    @Operation(summary = "Confirmar una interacción pendiente")
    @PostMapping("/{interactionId}/confirm")
    public ResponseEntity<InteractionResponse> confirm(@PathVariable Long interactionId) {
        return ResponseEntity.ok(interactionService.confirm(interactionId));
    }

    @Operation(summary = "Rechazar una interacción pendiente")
    @PostMapping("/{interactionId}/reject")
    public ResponseEntity<InteractionResponse> reject(@PathVariable Long interactionId) {
        return ResponseEntity.ok(interactionService.reject(interactionId));
    }

    @Operation(summary = "Interacciones pendientes de mi confirmación")
    @GetMapping("/pending")
    public ResponseEntity<List<InteractionResponse>> pending() {
        return ResponseEntity.ok(interactionService.listPendingForMe());
    }

    @Operation(summary = "Timeline de momentos confirmados")
    @GetMapping("/timeline")
    public ResponseEntity<PageResponse<InteractionResponse>> timeline(@PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(interactionService.timeline(pageable));
    }
}
