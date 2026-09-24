package com.mutuals.economy.repository;

import com.mutuals.economy.entity.WalletCurrency;
import com.mutuals.economy.entity.WalletTransaction;
import com.mutuals.economy.entity.WalletTransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;

public interface WalletTransactionRepository extends JpaRepository<WalletTransaction, Long> {

    Page<WalletTransaction> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    @Query("""
            select coalesce(sum(t.amount), 0) from WalletTransaction t
            where t.user.id = :userId and t.currency = :currency and t.type = :type
              and t.createdAt between :from and :to
            """)
    Long sumAmount(@Param("userId") Long userId, @Param("currency") WalletCurrency currency,
                   @Param("type") WalletTransactionType type, @Param("from") Instant from, @Param("to") Instant to);

    long countByUserIdAndCurrencyAndTypeAndCreatedAtBetween(Long userId, WalletCurrency currency,
                                                           WalletTransactionType type, Instant from, Instant to);

    long countByCurrencyAndTypeAndCreatedAtBetween(WalletCurrency currency, WalletTransactionType type,
                                                   Instant from, Instant to);
}
