package com.mutuals.avatar.repository;

import com.mutuals.avatar.entity.AvatarItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AvatarItemRepository extends JpaRepository<AvatarItem, Long> {

    List<AvatarItem> findByActiveTrueOrderByLayerAscPriceGemsAsc();

    boolean existsByCode(String code);
}
