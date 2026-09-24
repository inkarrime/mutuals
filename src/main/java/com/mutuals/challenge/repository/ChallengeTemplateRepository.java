package com.mutuals.challenge.repository;

import com.mutuals.challenge.entity.ChallengeTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChallengeTemplateRepository extends JpaRepository<ChallengeTemplate, Long> {

    List<ChallengeTemplate> findByActiveTrue();
}
