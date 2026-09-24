package com.mutuals.streak.repository;

import com.mutuals.streak.entity.Interaction;
import com.mutuals.streak.entity.InteractionMethod;
import com.mutuals.streak.entity.InteractionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

public interface InteractionRepository extends JpaRepository<Interaction, Long> {

    boolean existsByStreakIdAndStatus(Long streakId, InteractionStatus status);

    boolean existsByStreakIdAndStatusAndInteractionDateAndMethodIn(Long streakId, InteractionStatus status,
                                                                  LocalDate interactionDate,
                                                                  Collection<InteractionMethod> methods);

    List<Interaction> findByStatusAndExpiresAtBefore(InteractionStatus status, Instant now);

    @Query("""
            select i from Interaction i
            join fetch i.initiator
            join fetch i.streak s
            join fetch s.mutual m
            where i.status = :status
              and i.initiator.id <> :userId
              and (m.userA.id = :userId or m.userB.id = :userId)
            order by i.createdAt desc
            """)
    List<Interaction> findPendingForRecipient(@Param("userId") Long userId, @Param("status") InteractionStatus status);

    @Query(value = """
            select i from Interaction i
            where i.status = :status and (i.initiator.id = :userId or i.confirmer.id = :userId)
            order by i.confirmedAt desc
            """,
            countQuery = """
                    select count(i) from Interaction i
                    where i.status = :status and (i.initiator.id = :userId or i.confirmer.id = :userId)
                    """)
    Page<Interaction> findTimeline(@Param("userId") Long userId, @Param("status") InteractionStatus status,
                                   Pageable pageable);

    @Query("""
            select count(i) from Interaction i
            where i.status = :status and (i.initiator.id = :userId or i.confirmer.id = :userId)
              and i.interactionDate between :from and :to
            """)
    long countForUser(@Param("userId") Long userId, @Param("status") InteractionStatus status,
                      @Param("from") LocalDate from, @Param("to") LocalDate to);

    @Query("""
            select i.method, count(i) from Interaction i
            where i.status = :status and (i.initiator.id = :userId or i.confirmer.id = :userId)
              and i.interactionDate between :from and :to
            group by i.method
            """)
    List<Object[]> countByMethodForUser(@Param("userId") Long userId, @Param("status") InteractionStatus status,
                                        @Param("from") LocalDate from, @Param("to") LocalDate to);

    @Query("""
            select count(distinct i.interactionDate) from Interaction i
            where i.status = :status and (i.initiator.id = :userId or i.confirmer.id = :userId)
              and i.interactionDate between :from and :to
            """)
    long countActiveDaysForUser(@Param("userId") Long userId, @Param("status") InteractionStatus status,
                                @Param("from") LocalDate from, @Param("to") LocalDate to);

    @Query("""
            select i.streak.mutual.id, count(i) from Interaction i
            where i.status = :status and (i.initiator.id = :userId or i.confirmer.id = :userId)
              and i.interactionDate between :from and :to
            group by i.streak.mutual.id
            order by count(i) desc
            """)
    List<Object[]> countByMutualForUser(@Param("userId") Long userId, @Param("status") InteractionStatus status,
                                        @Param("from") LocalDate from, @Param("to") LocalDate to);

    @Query("""
            select count(i) from Interaction i
            where i.status = :status and i.streak.mutual.id = :mutualId
            """)
    long countForMutual(@Param("mutualId") Long mutualId, @Param("status") InteractionStatus status);

    @Query("""
            select i.method, count(i) from Interaction i
            where i.status = :status and i.streak.mutual.id = :mutualId
            group by i.method
            """)
    List<Object[]> countByMethodForMutual(@Param("mutualId") Long mutualId, @Param("status") InteractionStatus status);

    @Query("""
            select count(i) from Interaction i
            where i.status = :status and i.interactionDate between :from and :to
            """)
    long countPlatform(@Param("status") InteractionStatus status, @Param("from") LocalDate from, @Param("to") LocalDate to);

    @Query("""
            select i.method, count(i) from Interaction i
            where i.status = :status and i.interactionDate between :from and :to
            group by i.method
            """)
    List<Object[]> countByMethodPlatform(@Param("status") InteractionStatus status,
                                         @Param("from") LocalDate from, @Param("to") LocalDate to);

    @Query("""
            select i.interactionDate, count(i) from Interaction i
            where i.status = :status and i.interactionDate between :from and :to
            group by i.interactionDate
            order by count(i) desc
            """)
    List<Object[]> countByDayPlatform(@Param("status") InteractionStatus status,
                                      @Param("from") LocalDate from, @Param("to") LocalDate to);
}
