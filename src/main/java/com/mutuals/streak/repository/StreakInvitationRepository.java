package com.mutuals.streak.repository;

import com.mutuals.streak.entity.InvitationStatus;
import com.mutuals.streak.entity.StreakInvitation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StreakInvitationRepository extends JpaRepository<StreakInvitation, Long> {

    boolean existsByMutualIdAndStatus(Long mutualId, InvitationStatus status);

    Optional<StreakInvitation> findFirstByMutualIdAndStatus(Long mutualId, InvitationStatus status);

    List<StreakInvitation> findByInviteeIdAndStatusOrderByCreatedAtDesc(Long inviteeId, InvitationStatus status);

    List<StreakInvitation> findByInviterIdAndStatusOrderByCreatedAtDesc(Long inviterId, InvitationStatus status);
}
