package com.mutuals.activity.controller;

import com.mutuals.activity.dto.ActivityResponse;
import com.mutuals.activity.dto.CommentRequest;
import com.mutuals.activity.dto.CommentResponse;
import com.mutuals.activity.service.ActivityFeedService;
import com.mutuals.common.dto.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Activities", description = "Novedades: logros de mutuals y perfiles seguidos, likes y comentarios")
@RestController
@RequestMapping("/api/v1/activities")
@RequiredArgsConstructor
public class ActivityController {

    private final ActivityFeedService feedService;

    @Operation(summary = "Feed de novedades (mutuals y perfiles que sigo)")
    @GetMapping
    public ResponseEntity<PageResponse<ActivityResponse>> feed(@PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(feedService.feed(pageable));
    }

    @Operation(summary = "Dar like a una novedad")
    @PostMapping("/{activityId}/likes")
    public ResponseEntity<ActivityResponse> like(@PathVariable Long activityId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(feedService.like(activityId));
    }

    @Operation(summary = "Quitar like")
    @DeleteMapping("/{activityId}/likes")
    public ResponseEntity<Void> unlike(@PathVariable Long activityId) {
        feedService.unlike(activityId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Comentarios de una novedad")
    @GetMapping("/{activityId}/comments")
    public ResponseEntity<PageResponse<CommentResponse>> comments(@PathVariable Long activityId,
                                                                  @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(feedService.comments(activityId, pageable));
    }

    @Operation(summary = "Comentar (solo entre mutuals)")
    @PostMapping("/{activityId}/comments")
    public ResponseEntity<CommentResponse> comment(@PathVariable Long activityId,
                                                   @Valid @RequestBody CommentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(feedService.comment(activityId, request));
    }
}
