package com.mutuals.achievement.repository;

import com.mutuals.achievement.entity.UserAchievement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserAchievementRepository extends JpaRepository<UserAchievement, Long> {

    boolean existsByUserIdAndAchievementId(Long userId, Long achievementId);

    long countByUserId(Long userId);

    @Query("""
            select ua from UserAchievement ua
            join fetch ua.achievement
            where ua.user.id = :userId
            order by ua.unlockedAt desc
            """)
    List<UserAchievement> findAllByUserWithAchievement(@Param("userId") Long userId);
}
