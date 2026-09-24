package com.mutuals.subscription.controller;

import com.mutuals.subscription.dto.SubscribeRequest;
import com.mutuals.subscription.dto.SubscriptionResponse;
import com.mutuals.subscription.service.SubscriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Subscriptions", description = "Mutuals Plus (pago simulado para la demo)")
@RestController
@RequestMapping("/api/v1/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @Operation(summary = "Mi plan actual y sus beneficios")
    @GetMapping("/me")
    public ResponseEntity<SubscriptionResponse> current() {
        return ResponseEntity.ok(subscriptionService.current());
    }

    @Operation(summary = "Suscribirse a Mutuals Plus")
    @PostMapping
    public ResponseEntity<SubscriptionResponse> subscribe(@Valid @RequestBody SubscribeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(subscriptionService.subscribe(request.billingPeriod()));
    }

    @Operation(summary = "Cancelar la renovación (los beneficios siguen hasta el vencimiento)")
    @DeleteMapping("/me")
    public ResponseEntity<SubscriptionResponse> cancel() {
        return ResponseEntity.ok(subscriptionService.cancel());
    }
}
