package com.mutuals.economy.service;

import com.mutuals.common.dto.PageResponse;
import com.mutuals.config.AppProperties;
import com.mutuals.economy.dto.WalletResponse;
import com.mutuals.economy.dto.WalletTransactionResponse;
import com.mutuals.economy.mapper.WalletMapper;
import com.mutuals.economy.repository.WalletTransactionRepository;
import com.mutuals.event.ShieldGiftedEvent;
import com.mutuals.security.CurrentUserService;
import com.mutuals.social.entity.Mutual;
import com.mutuals.social.service.MutualService;
import com.mutuals.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ShieldService {

    private final WalletService walletService;
    private final WalletTransactionRepository transactionRepository;
    private final MutualService mutualService;
    private final CurrentUserService currentUserService;
    private final WalletMapper walletMapper;
    private final AppProperties properties;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional(readOnly = true)
    public WalletResponse getWallet() {
        User me = currentUserService.getCurrentUser();
        return new WalletResponse(me.getGems(), me.getShields(), properties.shop().shieldPriceGems());
    }

    @Transactional(readOnly = true)
    public PageResponse<WalletTransactionResponse> transactions(Pageable pageable) {
        return PageResponse.from(transactionRepository.findByUserIdOrderByCreatedAtDesc(
                currentUserService.getCurrentUserId(), pageable), walletMapper::toResponse);
    }

    @Transactional
    public WalletResponse purchase(int quantity) {
        User me = currentUserService.getCurrentUser();
        int price = properties.shop().shieldPriceGems();
        walletService.spendGems(me, price * quantity, "Purchased " + quantity + " shield(s)");
        walletService.grantShields(me, quantity, "Shield purchase");
        return new WalletResponse(me.getGems(), me.getShields(), price);
    }

    @Transactional
    public WalletResponse gift(Long friendId) {
        User me = currentUserService.getCurrentUser();
        Mutual mutual = mutualService.getActiveBetween(me.getId(), friendId);
        User friend = mutual.other(me.getId());
        walletService.transferShield(me, friend);
        eventPublisher.publishEvent(new ShieldGiftedEvent(me.getId(), friend.getId()));
        return new WalletResponse(me.getGems(), me.getShields(), properties.shop().shieldPriceGems());
    }
}
