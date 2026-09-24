package com.mutuals.activity.mapper;

import com.mutuals.activity.dto.ActivityResponse;
import com.mutuals.activity.dto.CommentResponse;
import com.mutuals.activity.entity.Activity;
import com.mutuals.activity.entity.ActivityComment;
import com.mutuals.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ActivityMapper {

    private final UserMapper userMapper;

    public ActivityResponse toResponse(Activity activity, boolean likedByMe, boolean fromMutual, boolean canComment) {
        return new ActivityResponse(
                activity.getId(),
                userMapper.toSummary(activity.getActor()),
                activity.getType(),
                activity.getTitle(),
                activity.getValue(),
                activity.getLikeCount(),
                activity.getCommentCount(),
                likedByMe,
                fromMutual,
                canComment,
                activity.getCreatedAt());
    }

    public CommentResponse toComment(ActivityComment comment) {
        return new CommentResponse(comment.getId(), userMapper.toSummary(comment.getUser()), comment.getContent(),
                comment.getCreatedAt());
    }
}
