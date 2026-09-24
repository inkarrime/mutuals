package com.mutuals.streak.repository;

import com.mutuals.streak.entity.Streak;
import com.mutuals.streak.entity.StreakStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface StreakRepository extends JpaRepository<Streak, Long> {

    Optional<Streak> findFirstByMutualIdAndStatus(Long mutualId, StreakStatus status);

    List<Streak> findByMutualIdOrderByStartDateDesc(Long mutualId);

    @Query("""
            select count(s) from Streak s
            where s.status = :status
              and (s.mutual.userA.id = :userId or s.mutual.userB.id = :userId)
            """)
    long countByUserAndStatus(@Param("userId") Long userId, @Param("status") StreakStatus status);

    @Query("""
            select s from Streak s
            join fetch s.mutual m
            join fetch m.userA
            join fetch m.userB
            where s.status = :status
              and ((s.lastActiveDate is null and s.startDate < :yesterday) or s.lastActiveDate < :yesterday)
            """)
    List<Streak> findMissed(@Param("status") StreakStatus status, @Param("yesterday") LocalDate yesterday);

    @Query("""
            select s from Streak s
            join fetch s.mutual m
            join fetch m.userA
            join fetch m.userB
            where s.status = :status and s.lastActiveDate = :date and s.currentLength > 0
            """)
    List<Streak> findAtRisk(@Param("status") StreakStatus status, @Param("date") LocalDate yesterday);

    @Query("""
            select s from Streak s
            join fetch s.mutual m
            join fetch m.userA
            join fetch m.userB
            where s.status = :status
            """)
    List<Streak> findAllByStatusWithMutual(@Param("status") StreakStatus status);

    @Query("""
            select s from Streak s
            join fetch s.mutual m
            join fetch m.userA
            join fetch m.userB
            where m.userA.id = :userId or m.userB.id = :userId
            order by s.startDate desc
            """)
    List<Streak> findAllByUser(@Param("userId") Long userId);

    @Query("""
            select coalesce(max(s.currentLength), 0) from Streak s
            where (s.mutual.userA.id = :userId or s.mutual.userB.id = :userId)
              and s.startDate <= :to and (s.endDate is null or s.endDate >= :from)
            """)
    Integer findLongestInRange(@Param("userId") Long userId, @Param("from") LocalDate from, @Param("to") LocalDate to);

    @Query("""
            select coalesce(max(s.currentLength), 0) from Streak s
            where s.startDate <= :to and (s.endDate is null or s.endDate >= :from)
            """)
    Integer findPlatformLongestInRange(@Param("from") LocalDate from, @Param("to") LocalDate to);

    long countByStatus(StreakStatus status);
}
