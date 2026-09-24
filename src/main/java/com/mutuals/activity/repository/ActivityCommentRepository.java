package com.mutuals.activity.repository;

import com.mutuals.activity.entity.ActivityComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ActivityCommentRepository extends JpaRepository<ActivityComment, Long> {

    Page<ActivityComment> findByActivityIdAndHiddenFalseOrderByCreatedAtAsc(Long activityId, Pageable pageable);
}
