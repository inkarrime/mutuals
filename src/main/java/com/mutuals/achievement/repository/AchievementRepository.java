package com.mutuals.achievement.repository;

import com.mutuals.achievement.entity.Achievement;
import com.mutuals.achievement.entity.AchievementType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AchievementRepository extends JpaRepository<Achievement, Long> {

    Optional<Achievement> findByCode(String code);

    boolean existsByCode(String code);

    List<Achievement> findByTypeAndThresholdLessThanEqual(AchievementType type, int threshold);
}
