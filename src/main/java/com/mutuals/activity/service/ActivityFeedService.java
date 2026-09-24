package com.mutuals.activity.service;

import com.mutuals.activity.dto.ActivityResponse;
import com.mutuals.activity.dto.CommentRequest;
import com.mutuals.activity.dto.CommentResponse;
import com.mutuals.activity.entity.Activity;
import com.mutuals.activity.entity.ActivityComment;
import com.mutuals.activity.entity.ActivityLike;
import com.mutuals.activity.mapper.ActivityMapper;
import com.mutuals.activity.repository.ActivityCommentRepository;
import com.mutuals.activity.repository.ActivityLikeRepository;
import com.mutuals.activity.repository.ActivityRepository;
import com.mutuals.common.dto.PageResponse;
import com.mutuals.common.exception.DuplicateResourceException;
import com.mutuals.common.exception.ForbiddenOperationException;
import com.mutuals.common.exception.ResourceNotFoundException;
import com.mutuals.common.exception.UserBlockedException;
import com.mutuals.event.ActivityCommentedEvent;
import com.mutuals.event.ActivityLikedEvent;
import com.mutuals.security.CurrentUserService;
import com.mutuals.social.entity.MutualStatus;
import com.mutuals.social.repository.BlockRepository;
import com.mutuals.social.repository.FollowRepository;
import com.mutuals.social.repository.MutualRepository;
import com.mutuals.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ActivityFeedService {

    private final ActivityRepository activityRepository;
    private final ActivityLikeRepository likeRepository;
    private final ActivityCommentRepository commentRepository;
    private final FollowRepository followRepository;
    private final MutualRepository mutualRepository;
    private final BlockRepository blockRepository;
    private final CurrentUserService currentUserService;
    private final ActivityMapper activityMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional(readOnly = true)
    public PageResponse<ActivityResponse> feed(Pageable pageable) {
        Long userId = currentUserService.getCurrentUserId();
        Set<Long> actorIds = new HashSet<>(followRepository.findFollowingIds(userId));
        actorIds.add(userId);
        Set<Long> mutualIds = mutualIdsOf(userId);
        Page<Activity> page = activityRepository.findFeed(actorIds, pageable);
        List<Long> activityIds = page.getContent().stream().map(Activity::getId).toList();
        Set<Long> liked = activityIds.isEmpty() ? Set.of() : likeRepository.findLikedActivityIds(userId, activityIds);
        return PageResponse.from(page, activity -> {
            Long actorId = activity.getActor().getId();
            boolean fromMutual = mutualIds.contains(actorId);
            return activityMapper.toResponse(activity, liked.contains(activity.getId()), fromMutual,
                    fromMutual || actorId.equals(userId));
        });
    }

    @Transactional
    public ActivityResponse like(Long activityId) {
        User me = currentUserService.getCurrentUser();
        Activity activity = requireVisible(activityId, me.getId());
        if (likeRepository.existsByUserIdAndActivityId(me.getId(), activityId)) {
            throw new DuplicateResourceException("You already liked this activity");
        }
        likeRepository.save(new ActivityLike(me, activity));
        activity.setLikeCount(activity.getLikeCount() + 1);
        eventPublisher.publishEvent(new ActivityLikedEvent(activityId, me.getId()));
        return toResponse(activity, me.getId(), true);
    }

    @Transactional
    public void unlike(Long activityId) {
        Long userId = currentUserService.getCurrentUserId();
        ActivityLike like = likeRepository.findByUserIdAndActivityId(userId, activityId)
                .orElseThrow(() -> new ResourceNotFoundException("Like on activity " + activityId + " not found"));
        like.getActivity().setLikeCount(Math.max(0, like.getActivity().getLikeCount() - 1));
        likeRepository.delete(like);
    }

    @Transactional
    public CommentResponse comment(Long activityId, CommentRequest request) {
        User me = currentUserService.getCurrentUser();
        Activity activity = requireVisible(activityId, me.getId());
        Long actorId = activity.getActor().getId();
        if (!actorId.equals(me.getId()) && !mutualIdsOf(me.getId()).contains(actorId)) {
            throw new ForbiddenOperationException("Only mutuals can comment on this activity");
        }
        ActivityComment comment = new ActivityComment();
        comment.setUser(me);
        comment.setActivity(activity);
        comment.setContent(request.content().trim());
        ActivityComment saved = commentRepository.save(comment);
        activity.setCommentCount(activity.getCommentCount() + 1);
        eventPublisher.publishEvent(new ActivityCommentedEvent(activityId, saved.getId(), me.getId()));
        return activityMapper.toComment(saved);
    }

    @Transactional(readOnly = true)
    public PageResponse<CommentResponse> comments(Long activityId, Pageable pageable) {
        requireVisible(activityId, currentUserService.getCurrentUserId());
        return PageResponse.from(commentRepository.findByActivityIdAndHiddenFalseOrderByCreatedAtAsc(activityId, pageable),
                activityMapper::toComment);
    }

    @Transactional
    public void hideComment(Long commentId) {
        ActivityComment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment", commentId));
        comment.setHidden(true);
        comment.getActivity().setCommentCount(Math.max(0, comment.getActivity().getCommentCount() - 1));
    }

    private Activity requireVisible(Long activityId, Long userId) {
        Activity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new ResourceNotFoundException("Activity", activityId));
        if (blockRepository.existsBetween(userId, activity.getActor().getId())) {
            throw new UserBlockedException();
        }
        return activity;
    }

    private ActivityResponse toResponse(Activity activity, Long userId, boolean likedByMe) {
        boolean fromMutual = mutualIdsOf(userId).contains(activity.getActor().getId());
        return activityMapper.toResponse(activity, likedByMe, fromMutual,
                fromMutual || activity.getActor().getId().equals(userId));
    }

    private Set<Long> mutualIdsOf(Long userId) {
        return mutualRepository.findByUserAndStatus(userId, MutualStatus.ACTIVE).stream()
                .map(mutual -> mutual.other(userId).getId())
                .collect(Collectors.toSet());
    }
}
