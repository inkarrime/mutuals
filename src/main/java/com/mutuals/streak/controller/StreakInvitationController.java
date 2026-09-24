package com.mutuals.streak.controller;

import com.mutuals.streak.dto.CreateInvitationRequest;
import com.mutuals.streak.dto.InvitationResponse;
import com.mutuals.streak.dto.RespondInvitationRequest;
import com.mutuals.streak.service.StreakInvitationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Streak invitations", description = "Invitar a un mutual a mantener una racha")
@RestController
@RequestMapping("/api/v1/streak-invitations")
@RequiredArgsConstructor
public class StreakInvitationController {

    private final StreakInvitationService invitationService;

    @Operation(summary = "Invitar a un mutual a una racha (plan gratis: hasta 4 rachas activas)")
    @PostMapping
    public ResponseEntity<InvitationResponse> invite(@Valid @RequestBody CreateInvitationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(invitationService.invite(request.friendId()));
    }

    @Operation(summary = "Invitaciones pendientes recibidas o enviadas")
    @GetMapping
    public ResponseEntity<List<InvitationResponse>> list(@RequestParam(defaultValue = "received") String box) {
        return ResponseEntity.ok("sent".equalsIgnoreCase(box) ? invitationService.listSent() : invitationService.listReceived());
    }

    @Operation(summary = "Aceptar o rechazar una invitación")
    @PatchMapping("/{invitationId}")
    public ResponseEntity<InvitationResponse> respond(@PathVariable Long invitationId,
                                                      @Valid @RequestBody RespondInvitationRequest request) {
        return ResponseEntity.ok(invitationService.respond(invitationId, request.accept()));
    }

    @Operation(summary = "Cancelar una invitación enviada")
    @DeleteMapping("/{invitationId}")
    public ResponseEntity<Void> cancel(@PathVariable Long invitationId) {
        invitationService.cancel(invitationId);
        return ResponseEntity.noContent().build();
    }
}
