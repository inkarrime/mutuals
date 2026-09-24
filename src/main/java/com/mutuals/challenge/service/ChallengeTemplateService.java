package com.mutuals.challenge.service;

import com.mutuals.challenge.dto.ChallengeTemplateRequest;
import com.mutuals.challenge.dto.ChallengeTemplateResponse;
import com.mutuals.challenge.entity.ChallengeTemplate;
import com.mutuals.challenge.mapper.ChallengeMapper;
import com.mutuals.challenge.repository.ChallengeTemplateRepository;
import com.mutuals.common.dto.PageResponse;
import com.mutuals.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChallengeTemplateService {

    private final ChallengeTemplateRepository templateRepository;
    private final ChallengeMapper challengeMapper;

    @Transactional(readOnly = true)
    public PageResponse<ChallengeTemplateResponse> list(Pageable pageable) {
        return PageResponse.from(templateRepository.findAll(pageable), challengeMapper::toTemplate);
    }

    @Transactional
    public ChallengeTemplateResponse create(ChallengeTemplateRequest request) {
        ChallengeTemplate template = new ChallengeTemplate();
        apply(template, request);
        return challengeMapper.toTemplate(templateRepository.save(template));
    }

    @Transactional
    public ChallengeTemplateResponse update(Long templateId, ChallengeTemplateRequest request) {
        ChallengeTemplate template = templateRepository.findById(templateId)
                .orElseThrow(() -> new ResourceNotFoundException("Challenge template", templateId));
        apply(template, request);
        return challengeMapper.toTemplate(template);
    }

    @Transactional
    public void deactivate(Long templateId) {
        ChallengeTemplate template = templateRepository.findById(templateId)
                .orElseThrow(() -> new ResourceNotFoundException("Challenge template", templateId));
        template.setActive(false);
    }

    private void apply(ChallengeTemplate template, ChallengeTemplateRequest request) {
        template.setTitle(request.title());
        template.setPrompt(request.prompt());
        template.setType(request.type());
        template.setRewardGems(request.rewardGems());
        template.setRequiresPresence(request.requiresPresence());
        template.setRequiresPhoto(request.requiresPhoto());
        template.setActive(request.active());
    }
}
