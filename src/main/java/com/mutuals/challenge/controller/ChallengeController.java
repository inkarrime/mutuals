package com.mutuals.challenge.controller;

import com.mutuals.challenge.dto.ChallengeResponse;
import com.mutuals.challenge.service.ChallengeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "Challenges", description = "Desafíos semanales por pareja con revelación mutua")
@Validated
@RestController
@RequestMapping("/api/v1/challenges")
@RequiredArgsConstructor
public class ChallengeController {

    private final ChallengeService challengeService;

    @Operation(summary = "Desafíos de esta semana con mis mutuals")
    @GetMapping
    public ResponseEntity<List<ChallengeResponse>> myChallenges() {
        return ResponseEntity.ok(challengeService.myWeeklyChallenges());
    }

    @Operation(summary = "Detalle de un desafío (la respuesta del otro se revela cuando ambos cumplen)")
    @GetMapping("/{challengeId}")
    public ResponseEntity<ChallengeResponse> get(@PathVariable Long challengeId) {
        return ResponseEntity.ok(challengeService.get(challengeId));
    }

    @Operation(summary = "Enviar mi parte del desafío (texto y/o foto)")
    @PostMapping(value = "/{challengeId}/submissions", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ChallengeResponse> submit(@PathVariable Long challengeId,
                                                    @RequestParam(required = false) @Size(max = 1000) String content,
                                                    @RequestPart(required = false) MultipartFile photo) {
        return ResponseEntity.status(HttpStatus.CREATED).body(challengeService.submit(challengeId, content, photo));
    }
}
