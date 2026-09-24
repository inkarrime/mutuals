package com.mutuals.economy.mapper;

import com.mutuals.economy.dto.WalletTransactionResponse;
import com.mutuals.economy.entity.WalletTransaction;
import org.springframework.stereotype.Component;

@Component
public class WalletMapper {

    public WalletTransactionResponse toResponse(WalletTransaction transaction) {
        return new WalletTransactionResponse(
                transaction.getId(),
                transaction.getCurrency(),
                transaction.getAmount(),
                transaction.getType(),
                transaction.getReason(),
                transaction.getCounterparty() == null ? null : transaction.getCounterparty().getUsername(),
                transaction.getCreatedAt());
    }
}
