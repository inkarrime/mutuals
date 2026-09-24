package com.mutuals.avatar.controller;

import com.mutuals.avatar.dto.AvatarItemResponse;
import com.mutuals.avatar.service.AvatarService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Avatar", description = "Tienda de ítems y avatar por capas")
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class AvatarController {

    private final AvatarService avatarService;

    @Operation(summary = "Catálogo de ítems de avatar")
    @GetMapping("/avatar-items")
    public ResponseEntity<List<AvatarItemResponse>> shop() {
        return ResponseEntity.ok(avatarService.shop());
    }

    @Operation(summary = "Comprar un ítem con gemas")
    @PostMapping("/avatar-items/{itemId}/purchase")
    public ResponseEntity<AvatarItemResponse> purchase(@PathVariable Long itemId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(avatarService.purchase(itemId));
    }

    @Operation(summary = "Mis ítems")
    @GetMapping("/users/me/avatar")
    public ResponseEntity<List<AvatarItemResponse>> myItems() {
        return ResponseEntity.ok(avatarService.myItems());
    }

    @Operation(summary = "Equipar un ítem (reemplaza el de la misma capa)")
    @PutMapping("/users/me/avatar/{itemId}")
    public ResponseEntity<AvatarItemResponse> equip(@PathVariable Long itemId) {
        return ResponseEntity.ok(avatarService.equip(itemId));
    }
}
