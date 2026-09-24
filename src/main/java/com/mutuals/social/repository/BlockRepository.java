package com.mutuals.social.repository;

import com.mutuals.social.entity.Block;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BlockRepository extends JpaRepository<Block, Long> {

    boolean existsByBlockerIdAndBlockedId(Long blockerId, Long blockedId);

    Optional<Block> findByBlockerIdAndBlockedId(Long blockerId, Long blockedId);

    Page<Block> findByBlockerId(Long blockerId, Pageable pageable);

    @Query("""
            select count(b) from Block b
            where (b.blocker.id = :first and b.blocked.id = :second)
               or (b.blocker.id = :second and b.blocked.id = :first)
            """)
    long countBetween(@Param("first") Long first, @Param("second") Long second);

    default boolean existsBetween(Long first, Long second) {
        return countBetween(first, second) > 0;
    }

    @Query("select b.blocked.id from Block b where b.blocker.id = :userId")
    List<Long> findBlockedIds(@Param("userId") Long userId);
}
