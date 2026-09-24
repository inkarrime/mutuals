package com.mutuals.challenge.repository;

import com.mutuals.challenge.entity.ChallengeStatus;
import com.mutuals.challenge.entity.ChallengeSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;

public interface ChallengeSubmissionRepository extends JpaRepository<ChallengeSubmission, Long> {

    long countByUserIdAndChallengeStatus(Long userId, ChallengeStatus status);

    @Query("""
            select count(s) from ChallengeSubmission s
            where s.user.id = :userId and s.challenge.status = :status
              and s.submittedAt between :from and :to
            """)
    long countCompletedForUserBetween(@Param("userId") Long userId, @Param("status") ChallengeStatus status,
                                      @Param("from") Instant from, @Param("to") Instant to);

    @Query("""
            select count(s) from ChallengeSubmission s
            where s.user.id = :userId and s.challenge.status = :status and s.challenge.mutual.id = :mutualId
            """)
    long countCompletedForUserAndMutual(@Param("userId") Long userId, @Param("mutualId") Long mutualId,
                                        @Param("status") ChallengeStatus status);
}
