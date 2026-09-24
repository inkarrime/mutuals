package com.mutuals.admin.service;

import com.mutuals.admin.dto.AdminMetricsResponse;
import com.mutuals.auth.service.RefreshTokenService;
import com.mutuals.common.exception.InvalidOperationException;
import com.mutuals.common.exception.ResourceNotFoundException;
import com.mutuals.social.entity.MutualStatus;
import com.mutuals.social.entity.ReportStatus;
import com.mutuals.social.repository.MutualRepository;
import com.mutuals.social.repository.ReportRepository;
import com.mutuals.streak.entity.StreakStatus;
import com.mutuals.streak.repository.StreakRepository;
import com.mutuals.subscription.entity.SubscriptionPlan;
import com.mutuals.subscription.entity.SubscriptionStatus;
import com.mutuals.subscription.repository.SubscriptionRepository;
import com.mutuals.user.dto.MeResponse;
import com.mutuals.user.entity.User;
import com.mutuals.user.entity.UserStatus;
import com.mutuals.user.mapper.UserMapper;
import com.mutuals.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final MutualRepository mutualRepository;
    private final StreakRepository streakRepository;
    private final ReportRepository reportRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final RefreshTokenService refreshTokenService;
    private final UserMapper userMapper;

    @Transactional(readOnly = true)
    public AdminMetricsResponse metrics() {
        return new AdminMetricsResponse(
                userRepository.countByStatus(UserStatus.ACTIVE),
                userRepository.countByStatus(UserStatus.SUSPENDED),
                mutualRepository.countByStatus(MutualStatus.ACTIVE),
                streakRepository.countByStatus(StreakStatus.ACTIVE),
                streakRepository.countByStatus(StreakStatus.BROKEN),
                reportRepository.countByStatus(ReportStatus.PENDING),
                subscriptionRepository.countByStatusAndPlan(SubscriptionStatus.ACTIVE, SubscriptionPlan.PLUS));
    }

    @Transactional
    public MeResponse updateUserStatus(Long userId, UserStatus status) {
        if (status == UserStatus.DELETED) {
            throw new InvalidOperationException("Use account deletion to delete users");
        }
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User", userId));
        user.setStatus(status);
        if (status == UserStatus.SUSPENDED) {
            refreshTokenService.revokeAll(userId);
        }
        return userMapper.toMe(user);
    }
}
