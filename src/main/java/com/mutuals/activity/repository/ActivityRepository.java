package com.mutuals.activity.repository;

import com.mutuals.activity.entity.Activity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;

public interface ActivityRepository extends JpaRepository<Activity, Long> {

    @Query(value = """
            select a from Activity a
            join fetch a.actor
            where a.actor.id in :actorIds
            order by a.createdAt desc
            """,
            countQuery = "select count(a) from Activity a where a.actor.id in :actorIds")
    Page<Activity> findFeed(@Param("actorIds") Collection<Long> actorIds, Pageable pageable);

    Page<Activity> findByActorIdOrderByCreatedAtDesc(Long actorId, Pageable pageable);
}
