package com.mutuals.social.service;

import com.mutuals.common.dto.PageResponse;
import com.mutuals.common.exception.DuplicateResourceException;
import com.mutuals.common.exception.InvalidOperationException;
import com.mutuals.common.exception.ResourceNotFoundException;
import com.mutuals.security.CurrentUserService;
import com.mutuals.social.entity.Block;
import com.mutuals.social.entity.MutualEndReason;
import com.mutuals.social.repository.BlockRepository;
import com.mutuals.social.repository.FollowRepository;
import com.mutuals.user.dto.UserSummaryResponse;
import com.mutuals.user.entity.User;
import com.mutuals.user.mapper.UserMapper;
import com.mutuals.user.service.UserLookupService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BlockService {

    private final BlockRepository blockRepository;
    private final FollowRepository followRepository;
    private final MutualService mutualService;
    private final UserLookupService userLookupService;
    private final CurrentUserService currentUserService;
    private final UserMapper userMapper;

    @Transactional
    public void block(Long targetId) {
        User me = currentUserService.getCurrentUser();
        if (me.getId().equals(targetId)) {
            throw new InvalidOperationException("You cannot block yourself");
        }
        User target = userLookupService.getActiveUser(targetId);
        if (blockRepository.existsByBlockerIdAndBlockedId(me.getId(), targetId)) {
            throw new DuplicateResourceException("User is already blocked");
        }
        blockRepository.save(new Block(me, target));
        followRepository.findByFollowerIdAndFollowedId(me.getId(), targetId).ifPresent(followRepository::delete);
        followRepository.findByFollowerIdAndFollowedId(targetId, me.getId()).ifPresent(followRepository::delete);
        mutualService.endIfActive(me.getId(), targetId, MutualEndReason.BLOCK);
    }

    @Transactional
    public void unblock(Long targetId) {
        Block block = blockRepository.findByBlockerIdAndBlockedId(currentUserService.getCurrentUserId(), targetId)
                .orElseThrow(() -> new ResourceNotFoundException("User " + targetId + " is not blocked"));
        blockRepository.delete(block);
    }

    @Transactional(readOnly = true)
    public PageResponse<UserSummaryResponse> listBlocked(Pageable pageable) {
        return PageResponse.from(blockRepository.findByBlockerId(currentUserService.getCurrentUserId(), pageable),
                block -> userMapper.toSummary(block.getBlocked()));
    }
}
