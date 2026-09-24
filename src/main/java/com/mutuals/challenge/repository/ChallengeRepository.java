package com.mutuals.challenge.repository;

import com.mutuals.challenge.entity.Challenge;
import com.mutuals.challenge.entity.ChallengeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface ChallengeRepository extends JpaRepository<Challenge, Long> {

    boolean existsByMutualIdAndWeekStart(Long mutualId, LocalDate weekStart);

    @Query("""
            select c from Challenge c
            join fetch c.template
            join fetch c.mutual m
            join fetch m.userA
            join fetch m.userB
            where (m.userA.id = :userId or m.userB.id = :userId) and c.weekStart = :weekStart
            """)
    List<Challenge> findForUserAndWeek(@Param("userId") Long userId, @Param("weekStart") LocalDate weekStart);

    List<Challenge> findByStatusAndWeekStartBefore(ChallengeStatus status, LocalDate weekStart);
}
