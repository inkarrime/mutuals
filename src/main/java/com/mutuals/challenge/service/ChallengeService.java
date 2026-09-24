package com.mutuals.challenge.service;

import com.mutuals.challenge.dto.ChallengeResponse;
import com.mutuals.challenge.entity.Challenge;
import com.mutuals.challenge.entity.ChallengeStatus;
import com.mutuals.challenge.entity.ChallengeSubmission;
import com.mutuals.challenge.entity.ChallengeTemplate;
import com.mutuals.challenge.mapper.ChallengeMapper;
import com.mutuals.challenge.repository.ChallengeRepository;
import com.mutuals.challenge.repository.ChallengeTemplateRepository;
import com.mutuals.common.exception.DuplicateResourceException;
import com.mutuals.common.exception.ForbiddenOperationException;
import com.mutuals.common.exception.InvalidCheckInException;
import com.mutuals.common.exception.InvalidOperationException;
import com.mutuals.common.exception.ResourceNotFoundException;
import com.mutuals.economy.service.WalletService;
import com.mutuals.event.ChallengeAssignedEvent;
import com.mutuals.event.ChallengeCompletedEvent;
import com.mutuals.security.CurrentUserService;
import com.mutuals.social.entity.Mutual;
import com.mutuals.storage.StorageService;
import com.mutuals.streak.entity.InteractionMethod;
import com.mutuals.streak.entity.InteractionStatus;
import com.mutuals.streak.entity.Streak;
import com.mutuals.streak.entity.StreakStatus;
import com.mutuals.streak.repository.InteractionRepository;
import com.mutuals.streak.repository.StreakRepository;
import com.mutuals.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.EnumSet;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class ChallengeService {

    private static final Random RANDOM = new SecureRandom();

    private final ChallengeRepository challengeRepository;
    private final ChallengeTemplateRepository templateRepository;
    private final StreakRepository streakRepository;
    private final InteractionRepository interactionRepository;
    private final WalletService walletService;
    private final StorageService storageService;
    private final CurrentUserService currentUserService;
    private final ChallengeMapper challengeMapper;
    private final ApplicationEventPublisher eventPublisher;
    private final Clock clock;

    @Transactional(readOnly = true)
    public List<ChallengeResponse> myWeeklyChallenges() {
        Long userId = currentUserService.getCurrentUserId();
        return challengeRepository.findForUserAndWeek(userId, currentWeekStart()).stream()
                .map(challenge -> challengeMapper.toChallenge(challenge, userId))
                .toList();
    }

    @Transactional(readOnly = true)
    public ChallengeResponse get(Long challengeId) {
        Long userId = currentUserService.getCurrentUserId();
        return challengeMapper.toChallenge(requireMemberChallenge(challengeId, userId), userId);
    }

    @Transactional
    public ChallengeResponse submit(Long challengeId, String content, MultipartFile photo) {
        User me = currentUserService.getCurrentUser();
        Challenge challenge = requireMemberChallenge(challengeId, me.getId());
        ChallengeTemplate template = challenge.getTemplate();
        if (challenge.getStatus() != ChallengeStatus.OPEN) {
            throw new InvalidOperationException("This challenge is no longer open");
        }
        if (challenge.hasSubmissionFrom(me.getId())) {
            throw new DuplicateResourceException("You already completed your part of this challenge");
        }
        boolean hasPhoto = photo != null && !photo.isEmpty();
        if (template.isRequiresPhoto() && !hasPhoto) {
            throw new InvalidOperationException("This challenge requires a photo");
        }
        if ((content == null || content.isBlank()) && !hasPhoto) {
            throw new InvalidOperationException("Add an answer or a photo to complete the challenge");
        }
        if (template.isRequiresPresence()) {
            assertCheckedInTogetherToday(challenge.getMutual());
        }
        ChallengeSubmission submission = new ChallengeSubmission();
        submission.setUser(me);
        submission.setContent(content);
        submission.setPhotoUrl(hasPhoto ? storageService.store(photo, "challenges") : null);
        submission.setSubmittedAt(clock.instant());
        challenge.addSubmission(submission);
        if (challenge.getSubmissions().size() == 2) {
            completeChallenge(challenge);
        }
        return challengeMapper.toChallenge(challengeRepository.save(challenge), me.getId());
    }

    @Transactional
    public int assignWeeklyChallenges(LocalDate weekStart) {
        List<ChallengeTemplate> templates = templateRepository.findByActiveTrue();
        if (templates.isEmpty()) {
            return 0;
        }
        int assigned = 0;
        for (Streak streak : streakRepository.findAllByStatusWithMutual(StreakStatus.ACTIVE)) {
            Mutual mutual = streak.getMutual();
            if (challengeRepository.existsByMutualIdAndWeekStart(mutual.getId(), weekStart)) {
                continue;
            }
            Challenge challenge = new Challenge();
            challenge.setMutual(mutual);
            challenge.setTemplate(templates.get(RANDOM.nextInt(templates.size())));
            challenge.setWeekStart(weekStart);
            Challenge saved = challengeRepository.save(challenge);
            eventPublisher.publishEvent(new ChallengeAssignedEvent(saved.getId(), mutual.getUserA().getId(),
                    mutual.getUserB().getId()));
            assigned++;
        }
        return assigned;
    }

    @Transactional
    public int expirePreviousWeeks(LocalDate weekStart) {
        List<Challenge> stale = challengeRepository.findByStatusAndWeekStartBefore(ChallengeStatus.OPEN, weekStart);
        stale.forEach(challenge -> challenge.setStatus(ChallengeStatus.EXPIRED));
        return stale.size();
    }

    public LocalDate currentWeekStart() {
        return LocalDate.now(clock).with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    }

    private void completeChallenge(Challenge challenge) {
        challenge.setStatus(ChallengeStatus.COMPLETED);
        challenge.setCompletedAt(clock.instant());
        Mutual mutual = challenge.getMutual();
        int reward = challenge.getTemplate().getRewardGems();
        String reason = "Challenge completed: " + challenge.getTemplate().getTitle();
        walletService.earnGems(mutual.getUserA(), reward, reason);
        walletService.earnGems(mutual.getUserB(), reward, reason);
        eventPublisher.publishEvent(new ChallengeCompletedEvent(challenge.getId(), mutual.getUserA().getId(),
                mutual.getUserB().getId(), reward));
    }

    private void assertCheckedInTogetherToday(Mutual mutual) {
        boolean together = streakRepository.findFirstByMutualIdAndStatus(mutual.getId(), StreakStatus.ACTIVE)
                .map(streak -> interactionRepository.existsByStreakIdAndStatusAndInteractionDateAndMethodIn(
                        streak.getId(), InteractionStatus.CONFIRMED, LocalDate.now(clock),
                        EnumSet.of(InteractionMethod.QR, InteractionMethod.PROXIMITY)))
                .orElse(false);
        if (!together) {
            throw new InvalidCheckInException("Check in together by QR or proximity before completing this challenge");
        }
    }

    private Challenge requireMemberChallenge(Long challengeId, Long userId) {
        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new ResourceNotFoundException("Challenge", challengeId));
        if (!challenge.getMutual().involves(userId)) {
            throw new ForbiddenOperationException("You are not part of this challenge");
        }
        return challenge;
    }
}
