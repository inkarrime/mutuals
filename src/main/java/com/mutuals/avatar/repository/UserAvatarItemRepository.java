package com.mutuals.avatar.repository;

import com.mutuals.avatar.entity.AvatarLayer;
import com.mutuals.avatar.entity.UserAvatarItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserAvatarItemRepository extends JpaRepository<UserAvatarItem, Long> {

    boolean existsByUserIdAndItemId(Long userId, Long itemId);

    Optional<UserAvatarItem> findByUserIdAndItemId(Long userId, Long itemId);

    List<UserAvatarItem> findByUserIdAndEquippedTrueAndItemLayer(Long userId, AvatarLayer layer);

    @Query("""
            select u from UserAvatarItem u
            join fetch u.item
            where u.user.id = :userId
            """)
    List<UserAvatarItem> findAllByUserWithItem(@Param("userId") Long userId);
}
