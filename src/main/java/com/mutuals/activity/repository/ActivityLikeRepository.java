package com.mutuals.activity.repository;

import com.mutuals.activity.entity.ActivityLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

public interface ActivityLikeRepository extends JpaRepository<ActivityLike, Long> {

    boolean existsByUserIdAndActivityId(Long userId, Long activityId);

    Optional<ActivityLike> findByUserIdAndActivityId(Long userId, Long activityId);

    @Query("select l.activity.id from ActivityLike l where l.user.id = :userId and l.activity.id in :activityIds")
    Set<Long> findLikedActivityIds(@Param("userId") Long userId, @Param("activityIds") Collection<Long> activityIds);
}
