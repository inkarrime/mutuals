package com.mutuals.streak.controller;

import com.mutuals.common.dto.MessageResponse;
import com.mutuals.streak.dto.InteractionResponse;
import com.mutuals.streak.dto.ProximityCheckInRequest;
import com.mutuals.streak.dto.ProximityCheckInResponse;
import com.mutuals.streak.dto.QrCheckInRequest;
import com.mutuals.streak.dto.QrCodeResponse;
import com.mutuals.streak.service.CheckInService;
import com.mutuals.streak.service.ProximityDetectionService;
import com.mutuals.user.dto.LocationUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Check-ins", description = "Verificación de presencia por QR, proximidad y detección por ubicación")
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class CheckInController {

    private final CheckInService checkInService;
    private final ProximityDetectionService proximityDetectionService;

    @Operation(summary = "Obtener mi código QR rotativo (vence cada 30 s)")
    @GetMapping("/qr-codes/me")
    public ResponseEntity<QrCodeResponse> myQrCode() {
        return ResponseEntity.ok(checkInService.myQrCode());
    }

    @Operation(summary = "Escanear el QR de un mutual y confirmar la interacción al instante")
    @PostMapping("/check-ins/qr")
    public ResponseEntity<InteractionResponse> checkInWithQr(@Valid @RequestBody QrCheckInRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(checkInService.checkInWithQr(request.token()));
    }

    @Operation(summary = "Check-in de proximidad: ambos envían su ubicación en menos de 2 minutos")
    @PostMapping("/check-ins/proximity")
    public ResponseEntity<ProximityCheckInResponse> checkInByProximity(
            @Valid @RequestBody ProximityCheckInRequest request) {
        ProximityCheckInResponse response = checkInService.checkInByProximity(request);
        HttpStatus status = response.status() == ProximityCheckInResponse.ProximityCheckInStatus.CONFIRMED
                ? HttpStatus.CREATED : HttpStatus.ACCEPTED;
        return ResponseEntity.status(status).body(response);
    }

    @Operation(summary = "Enviar mi ubicación para detectar mutuals cercanos (requiere consentimiento)")
    @PostMapping("/locations")
    public ResponseEntity<MessageResponse> updateLocation(@Valid @RequestBody LocationUpdateRequest request) {
        int detected = proximityDetectionService.updateLocation(request);
        return ResponseEntity.accepted().body(new MessageResponse(detected + " nearby mutuals detected"));
    }
}
