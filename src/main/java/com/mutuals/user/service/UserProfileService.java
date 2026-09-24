package com.mutuals.user.service;

import com.mutuals.achievement.repository.UserAchievementRepository;
import com.mutuals.common.dto.PageResponse;
import com.mutuals.security.CurrentUserService;
import com.mutuals.social.repository.FollowRepository;
import com.mutuals.user.dto.MeResponse;
import com.mutuals.user.dto.PublicProfileResponse;
import com.mutuals.user.dto.UpdateProfileRequest;
import com.mutuals.user.dto.UserSummaryResponse;
import com.mutuals.user.entity.Role;
import com.mutuals.user.entity.User;
import com.mutuals.user.entity.UserStatus;
import com.mutuals.user.mapper.UserMapper;
import com.mutuals.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final CurrentUserService currentUserService;
    private final UserLookupService userLookupService;
    private final UserRepository userRepository;
    private final FollowRepository followRepository;
    private final UserAchievementRepository userAchievementRepository;
    private final UserMapper userMapper;

    @Transactional(readOnly = true)
    public MeResponse getMe() {
        return userMapper.toMe(currentUserService.getCurrentUser());
    }

    @Transactional
    public MeResponse updateMe(UpdateProfileRequest request) {
        User user = currentUserService.getCurrentUser();
        if (request.displayName() != null) {
            user.setDisplayName(request.displayName().trim());
        }
        if (request.bio() != null) {
            user.setBio(request.bio().trim());
        }
        if (request.avatarUrl() != null) {
            user.setAvatarUrl(request.avatarUrl());
        }
        return userMapper.toMe(user);
    }

    @Transactional(readOnly = true)
    public PublicProfileResponse getPublicProfile(Long userId) {
        return toPublicProfile(userLookupService.getActiveUser(userId));
    }

    @Transactional(readOnly = true)
    public PublicProfileResponse getPublicProfileByUsername(String username) {
        return toPublicProfile(userLookupService.getByUsername(username));
    }

    @Transactional(readOnly = true)
    public PageResponse<UserSummaryResponse> search(String query, Pageable pageable) {
        String prefix = query.trim().toLowerCase(Locale.ROOT);
        return PageResponse.from(
                userRepository.findByUsernameStartingWithAndStatus(prefix, UserStatus.ACTIVE, pageable),
                userMapper::toSummary);
    }

    private PublicProfileResponse toPublicProfile(User user) {
        Long viewerId = currentUserService.getCurrentUserId();
        return new PublicProfileResponse(
                user.getId(),
                user.getUsername(),
                user.getDisplayName(),
                user.getBio(),
                user.getAvatarUrl(),
                user.getPersonalStreak(),
                user.getLongestPersonalStreak(),
                followRepository.countByFollowedId(user.getId()),
                followRepository.countByFollowerId(user.getId()),
                userAchievementRepository.countByUserId(user.getId()),
                followRepository.existsByFollowerIdAndFollowedId(viewerId, user.getId()),
                followRepository.existsByFollowerIdAndFollowedId(user.getId(), viewerId),
                user.hasRole(Role.PREMIUM),
                userMapper.toCustomization(user.getCustomization()),
                user.getCreatedAt());
    }
}
