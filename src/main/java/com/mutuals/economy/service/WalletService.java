package com.mutuals.economy.service;

import com.mutuals.common.exception.InsufficientBalanceException;
import com.mutuals.economy.entity.WalletCurrency;
import com.mutuals.economy.entity.WalletTransaction;
import com.mutuals.economy.entity.WalletTransactionType;
import com.mutuals.economy.repository.WalletTransactionRepository;
import com.mutuals.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WalletService {

    private final WalletTransactionRepository transactionRepository;

    @Transactional
    public void earnGems(User user, int amount, String reason) {
        if (amount <= 0) {
            return;
        }
        user.setGems(user.getGems() + amount);
        record(user, WalletCurrency.GEM, amount, WalletTransactionType.EARN, reason, null);
    }

    @Transactional
    public void spendGems(User user, int amount, String reason) {
        if (user.getGems() < amount) {
            throw new InsufficientBalanceException(WalletCurrency.GEM.name(), amount, user.getGems());
        }
        user.setGems(user.getGems() - amount);
        record(user, WalletCurrency.GEM, -amount, WalletTransactionType.SPEND, reason, null);
    }

    @Transactional
    public void grantShields(User user, int quantity, String reason) {
        user.setShields(user.getShields() + quantity);
        record(user, WalletCurrency.SHIELD, quantity, WalletTransactionType.GRANT, reason, null);
    }

    @Transactional
    public void useShield(User user, String reason) {
        requireShield(user);
        user.setShields(user.getShields() - 1);
        record(user, WalletCurrency.SHIELD, -1, WalletTransactionType.USED, reason, null);
    }

    @Transactional
    public void transferShield(User sender, User receiver) {
        requireShield(sender);
        sender.setShields(sender.getShields() - 1);
        receiver.setShields(receiver.getShields() + 1);
        record(sender, WalletCurrency.SHIELD, -1, WalletTransactionType.GIFT_SENT, "Shield gifted", receiver);
        record(receiver, WalletCurrency.SHIELD, 1, WalletTransactionType.GIFT_RECEIVED, "Shield received", sender);
    }

    private void requireShield(User user) {
        if (user.getShields() < 1) {
            throw new InsufficientBalanceException(WalletCurrency.SHIELD.name(), 1, user.getShields());
        }
    }

    private void record(User user, WalletCurrency currency, int amount, WalletTransactionType type, String reason,
                        User counterparty) {
        WalletTransaction transaction = new WalletTransaction();
        transaction.setUser(user);
        transaction.setCurrency(currency);
        transaction.setAmount(amount);
        transaction.setType(type);
        transaction.setReason(reason);
        transaction.setCounterparty(counterparty);
        transactionRepository.save(transaction);
    }
}
