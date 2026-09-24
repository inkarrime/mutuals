package com.mutuals.economy.dto;

import com.mutuals.economy.entity.WalletCurrency;
import com.mutuals.economy.entity.WalletTransactionType;

import java.time.Instant;

public record WalletTransactionResponse(
        Long id,
        WalletCurrency currency,
        int amount,
        WalletTransactionType type,
        String reason,
        String counterpartyUsername,
        Instant createdAt
) {
}
