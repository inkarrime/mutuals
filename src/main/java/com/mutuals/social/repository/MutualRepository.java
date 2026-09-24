package com.mutuals.social.repository;

import com.mutuals.social.entity.Mutual;
import com.mutuals.social.entity.MutualStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface MutualRepository extends JpaRepository<Mutual, Long> {

    Optional<Mutual> findByUserAIdAndUserBId(Long userAId, Long userBId);

    @Query("""
            select m from Mutual m
            join fetch m.userA
            join fetch m.userB
            where (m.userA.id = :userId or m.userB.id = :userId) and m.status = :status
            """)
    List<Mutual> findByUserAndStatus(@Param("userId") Long userId, @Param("status") MutualStatus status);

    @Query("""
            select m from Mutual m
            join fetch m.userA
            join fetch m.userB
            where m.userA.id = :userId or m.userB.id = :userId
            """)
    List<Mutual> findAllByUser(@Param("userId") Long userId);

    @Query("""
            select count(m) from Mutual m
            where (m.userA.id = :userId or m.userB.id = :userId)
              and m.currentSince between :from and :to
            """)
    long countStartedBetween(@Param("userId") Long userId, @Param("from") Instant from, @Param("to") Instant to);

    long countByStatus(MutualStatus status);
}
