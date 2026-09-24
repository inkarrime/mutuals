package com.mutuals.social.repository;

import com.mutuals.social.entity.Follow;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FollowRepository extends JpaRepository<Follow, Long> {

    boolean existsByFollowerIdAndFollowedId(Long followerId, Long followedId);

    Optional<Follow> findByFollowerIdAndFollowedId(Long followerId, Long followedId);

    Page<Follow> findByFollowedId(Long followedId, Pageable pageable);

    Page<Follow> findByFollowerId(Long followerId, Pageable pageable);

    long countByFollowedId(Long followedId);

    long countByFollowerId(Long followerId);

    @Modifying
    @Query("delete from Follow f where f.follower.id = :userId or f.followed.id = :userId")
    int deleteAllInvolving(@Param("userId") Long userId);

    @Query("select f.follower.id from Follow f where f.followed.id = :userId")
    List<Long> findFollowerIds(@Param("userId") Long userId);

    @Query("select f.followed.id from Follow f where f.follower.id = :userId")
    List<Long> findFollowingIds(@Param("userId") Long userId);
}
