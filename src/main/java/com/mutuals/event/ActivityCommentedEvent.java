package com.mutuals.event;


public record ActivityCommentedEvent(Long activityId, Long commentId, Long authorId) {
}
