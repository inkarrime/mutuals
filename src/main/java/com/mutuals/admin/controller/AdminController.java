package com.mutuals.admin.controller;

import com.mutuals.activity.service.ActivityFeedService;
import com.mutuals.admin.dto.AdminMetricsResponse;
import com.mutuals.admin.dto.UpdateUserStatusRequest;
import com.mutuals.admin.service.AdminService;
import com.mutuals.challenge.dto.ChallengeTemplateRequest;
import com.mutuals.challenge.dto.ChallengeTemplateResponse;
import com.mutuals.challenge.service.ChallengeService;
import com.mutuals.challenge.service.ChallengeTemplateService;
import com.mutuals.common.dto.MessageResponse;
import com.mutuals.common.dto.PageResponse;
import com.mutuals.social.dto.AdminReportResponse;
import com.mutuals.social.dto.ReviewReportRequest;
import com.mutuals.social.entity.ReportStatus;
import com.mutuals.social.service.ReportService;
import com.mutuals.user.dto.MeResponse;
import com.mutuals.wrapped.dto.WrappedPeriod;
import com.mutuals.wrapped.dto.WrappedResponse;
import com.mutuals.wrapped.service.WrappedService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Admin", description = "Moderación, catálogo de desafíos, métricas y Wrapped de la plataforma")
@Validated
@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final ReportService reportService;
    private final ChallengeTemplateService templateService;
    private final ChallengeService challengeService;
    private final ActivityFeedService activityFeedService;
    private final WrappedService wrappedService;

    @Operation(summary = "Métricas generales")
    @GetMapping("/metrics")
    public ResponseEntity<AdminMetricsResponse> metrics() {
        return ResponseEntity.ok(adminService.metrics());
    }

    @Operation(summary = "Reportes por estado")
    @GetMapping("/reports")
    public ResponseEntity<PageResponse<AdminReportResponse>> reports(
            @RequestParam(defaultValue = "PENDING") ReportStatus status,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(reportService.listByStatus(status, pageable));
    }

    @Operation(summary = "Revisar un reporte (opcionalmente suspender al usuario)")
    @PatchMapping("/reports/{reportId}")
    public ResponseEntity<AdminReportResponse> review(@PathVariable Long reportId,
                                                      @Valid @RequestBody ReviewReportRequest request) {
        return ResponseEntity.ok(reportService.review(reportId, request));
    }

    @Operation(summary = "Suspender o reactivar una cuenta")
    @PatchMapping("/users/{userId}/status")
    public ResponseEntity<MeResponse> updateUserStatus(@PathVariable Long userId,
                                                       @Valid @RequestBody UpdateUserStatusRequest request) {
        return ResponseEntity.ok(adminService.updateUserStatus(userId, request.status()));
    }

    @Operation(summary = "Ocultar un comentario")
    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<Void> hideComment(@PathVariable Long commentId) {
        activityFeedService.hideComment(commentId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Catálogo de plantillas de desafíos")
    @GetMapping("/challenge-templates")
    public ResponseEntity<PageResponse<ChallengeTemplateResponse>> templates(
            @PageableDefault(size = 50) Pageable pageable) {
        return ResponseEntity.ok(templateService.list(pageable));
    }

    @Operation(summary = "Crear plantilla de desafío")
    @PostMapping("/challenge-templates")
    public ResponseEntity<ChallengeTemplateResponse> createTemplate(@Valid @RequestBody ChallengeTemplateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(templateService.create(request));
    }

    @Operation(summary = "Editar plantilla de desafío")
    @PutMapping("/challenge-templates/{templateId}")
    public ResponseEntity<ChallengeTemplateResponse> updateTemplate(@PathVariable Long templateId,
                                                                    @Valid @RequestBody ChallengeTemplateRequest request) {
        return ResponseEntity.ok(templateService.update(templateId, request));
    }

    @Operation(summary = "Desactivar plantilla de desafío")
    @DeleteMapping("/challenge-templates/{templateId}")
    public ResponseEntity<Void> deactivateTemplate(@PathVariable Long templateId) {
        templateService.deactivate(templateId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Generar el Wrapped público de Mutuals (YYYY o YYYY-MM)")
    @PostMapping("/wrapped/platform/{periodKey}")
    public ResponseEntity<WrappedResponse> generatePlatformWrapped(
            @PathVariable @Pattern(regexp = "^\\d{4}(-\\d{2})?$") String periodKey) {
        return ResponseEntity.status(HttpStatus.CREATED).body(wrappedService.generatePlatform(WrappedPeriod.parse(periodKey)));
    }

    @Operation(summary = "Asignar los desafíos de la semana ahora (el job corre los lunes 00:10)")
    @PostMapping("/challenges/assign")
    public ResponseEntity<MessageResponse> assignChallenges() {
        int assigned = challengeService.assignWeeklyChallenges(challengeService.currentWeekStart());
        return ResponseEntity.ok(new MessageResponse(assigned + " challenges assigned for this week"));
    }
}
