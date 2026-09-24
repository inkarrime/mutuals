package com.mutuals.challenge.mapper;

import com.mutuals.challenge.dto.ChallengeResponse;
import com.mutuals.challenge.dto.ChallengeTemplateResponse;
import com.mutuals.challenge.dto.SubmissionResponse;
import com.mutuals.challenge.entity.Challenge;
import com.mutuals.challenge.entity.ChallengeStatus;
import com.mutuals.challenge.entity.ChallengeSubmission;
import com.mutuals.challenge.entity.ChallengeTemplate;
import com.mutuals.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChallengeMapper {

    private final UserMapper userMapper;

    public ChallengeTemplateResponse toTemplate(ChallengeTemplate template) {
        return new ChallengeTemplateResponse(template.getId(), template.getTitle(), template.getPrompt(),
                template.getType(), template.getRewardGems(), template.isRequiresPresence(),
                template.isRequiresPhoto(), template.isActive());
    }

    public ChallengeResponse toChallenge(Challenge challenge, Long viewerId) {
        boolean bothSubmitted = challenge.getStatus() == ChallengeStatus.COMPLETED;
        return new ChallengeResponse(
                challenge.getId(),
                toTemplate(challenge.getTemplate()),
                userMapper.toSummary(challenge.getMutual().other(viewerId)),
                challenge.getWeekStart(),
                challenge.getStatus(),
                challenge.hasSubmissionFrom(viewerId),
                challenge.getSubmissions().stream()
                        .map(submission -> toSubmission(submission, viewerId, bothSubmitted))
                        .toList());
    }

    private SubmissionResponse toSubmission(ChallengeSubmission submission, Long viewerId, boolean bothSubmitted) {
        boolean revealed = bothSubmitted || submission.getUser().getId().equals(viewerId);
        return new SubmissionResponse(
                userMapper.toSummary(submission.getUser()),
                revealed,
                revealed ? submission.getContent() : null,
                revealed ? submission.getPhotoUrl() : null,
                submission.getSubmittedAt());
    }
}
