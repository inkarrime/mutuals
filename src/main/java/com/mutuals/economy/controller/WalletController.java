package com.mutuals.economy.controller;

import com.mutuals.common.dto.PageResponse;
import com.mutuals.economy.dto.GiftShieldRequest;
import com.mutuals.economy.dto.PurchaseShieldsRequest;
import com.mutuals.economy.dto.WalletResponse;
import com.mutuals.economy.dto.WalletTransactionResponse;
import com.mutuals.economy.service.ShieldService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Wallet", description = "Gemas, escudos, compras y regalos")
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class WalletController {

    private final ShieldService shieldService;

    @Operation(summary = "Saldo de gemas y escudos")
    @GetMapping("/wallet")
    public ResponseEntity<WalletResponse> wallet() {
        return ResponseEntity.ok(shieldService.getWallet());
    }

    @Operation(summary = "Historial de movimientos de la billetera")
    @GetMapping("/wallet/transactions")
    public ResponseEntity<PageResponse<WalletTransactionResponse>> transactions(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(shieldService.transactions(pageable));
    }

    @Operation(summary = "Comprar escudos con gemas")
    @PostMapping("/shields/purchase")
    public ResponseEntity<WalletResponse> purchase(@Valid @RequestBody PurchaseShieldsRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(shieldService.purchase(request.quantity()));
    }

    @Operation(summary = "Regalar un escudo a un mutual")
    @PostMapping("/shields/gifts")
    public ResponseEntity<WalletResponse> gift(@Valid @RequestBody GiftShieldRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(shieldService.gift(request.friendId()));
    }
}
