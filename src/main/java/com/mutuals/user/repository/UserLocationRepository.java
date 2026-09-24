package com.mutuals.user.repository;

import com.mutuals.user.entity.UserLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface UserLocationRepository extends JpaRepository<UserLocation, Long> {

    Optional<UserLocation> findByUserId(Long userId);

    List<UserLocation> findByUserIdInAndRecordedAtAfter(Collection<Long> userIds, Instant after);

    @Modifying
    @Query("delete from UserLocation l where l.recordedAt < :before")
    int deleteOlderThan(@Param("before") Instant before);

    @Modifying
    @Query("delete from UserLocation l where l.user.id = :userId")
    int deleteByUserId(@Param("userId") Long userId);
}
