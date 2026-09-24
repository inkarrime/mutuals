package com.mutuals.social.service;

import com.mutuals.common.dto.PageResponse;
import com.mutuals.common.exception.ActiveStreakConflictException;
import com.mutuals.common.exception.DuplicateResourceException;
import com.mutuals.common.exception.InvalidOperationException;
import com.mutuals.common.exception.ResourceNotFoundException;
import com.mutuals.common.exception.UserBlockedException;
import com.mutuals.event.UserFollowedEvent;
import com.mutuals.security.CurrentUserService;
import com.mutuals.social.dto.FollowResponse;
import com.mutuals.social.entity.Follow;
import com.mutuals.social.entity.Mutual;
import com.mutuals.social.entity.MutualEndReason;
import com.mutuals.social.repository.BlockRepository;
import com.mutuals.social.repository.FollowRepository;
import com.mutuals.streak.entity.StreakStatus;
import com.mutuals.streak.repository.StreakRepository;
import com.mutuals.user.dto.UserSummaryResponse;
import com.mutuals.user.entity.User;
import com.mutuals.user.mapper.UserMapper;
import com.mutuals.user.service.UserLookupService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FollowService {

    private final FollowRepository followRepository;
    private final BlockRepository blockRepository;
    private final StreakRepository streakRepository;
    private final MutualService mutualService;
    private final UserLookupService userLookupService;
    private final CurrentUserService currentUserService;
    private final UserMapper userMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public FollowResponse follow(Long targetId) {
        User me = currentUserService.getCurrentUser();
        if (me.getId().equals(targetId)) {
            throw new InvalidOperationException("You cannot follow yourself");
        }
        User target = userLookupService.getActiveUser(targetId);
        if (blockRepository.existsBetween(me.getId(), targetId)) {
            throw new UserBlockedException();
        }
        if (followRepository.existsByFollowerIdAndFollowedId(me.getId(), targetId)) {
            throw new DuplicateResourceException("You already follow this user");
        }
        followRepository.save(new Follow(me, target));
        eventPublisher.publishEvent(new UserFollowedEvent(me.getId(), targetId));
        boolean mutual = followRepository.existsByFollowerIdAndFollowedId(targetId, me.getId());
        if (mutual) {
            mutualService.activate(me, target);
        }
        return new FollowResponse(targetId, true, mutual);
    }

    @Transactional
    public void unfollow(Long targetId, boolean confirmStreakLoss) {
        Long myId = currentUserService.getCurrentUserId();
        Follow follow = followRepository.findByFollowerIdAndFollowedId(myId, targetId)
                .orElseThrow(() -> new ResourceNotFoundException("You do not follow user " + targetId));
        Optional<Mutual> mutual = mutualService.findActiveBetween(myId, targetId);
        if (mutual.isPresent() && !confirmStreakLoss) {
            streakRepository.findFirstByMutualIdAndStatus(mutual.get().getId(), StreakStatus.ACTIVE)
                    .ifPresent(streak -> {
                        throw new ActiveStreakConflictException(streak.getId(), streak.getCurrentLength());
                    });
        }
        followRepository.delete(follow);
        mutual.ifPresent(active -> mutualService.end(active, MutualEndReason.UNFOLLOW));
    }

    @Transactional(readOnly = true)
    public PageResponse<UserSummaryResponse> followers(Long userId, Pageable pageable) {
        userLookupService.getActiveUser(userId);
        return PageResponse.from(followRepository.findByFollowedId(userId, pageable),
                follow -> userMapper.toSummary(follow.getFollower()));
    }

    @Transactional(readOnly = true)
    public PageResponse<UserSummaryResponse> following(Long userId, Pageable pageable) {
        userLookupService.getActiveUser(userId);
        return PageResponse.from(followRepository.findByFollowerId(userId, pageable),
                follow -> userMapper.toSummary(follow.getFollowed()));
    }
}
