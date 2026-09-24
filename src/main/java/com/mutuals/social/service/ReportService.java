package com.mutuals.social.service;

import com.mutuals.common.dto.PageResponse;
import com.mutuals.common.exception.DuplicateResourceException;
import com.mutuals.common.exception.InvalidOperationException;
import com.mutuals.common.exception.ResourceNotFoundException;
import com.mutuals.security.CurrentUserService;
import com.mutuals.social.dto.AdminReportResponse;
import com.mutuals.social.dto.CreateReportRequest;
import com.mutuals.social.dto.ReportResponse;
import com.mutuals.social.dto.ReviewReportRequest;
import com.mutuals.social.entity.Report;
import com.mutuals.social.entity.ReportStatus;
import com.mutuals.social.mapper.SocialMapper;
import com.mutuals.social.repository.ReportRepository;
import com.mutuals.user.entity.User;
import com.mutuals.user.entity.UserStatus;
import com.mutuals.user.service.UserLookupService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository reportRepository;
    private final UserLookupService userLookupService;
    private final CurrentUserService currentUserService;
    private final SocialMapper socialMapper;
    private final Clock clock;

    @Transactional
    public ReportResponse create(CreateReportRequest request) {
        User reporter = currentUserService.getCurrentUser();
        if (reporter.getId().equals(request.reportedUserId())) {
            throw new InvalidOperationException("You cannot report yourself");
        }
        User reported = userLookupService.getActiveUser(request.reportedUserId());
        if (reportRepository.existsByReporterIdAndReportedIdAndStatus(reporter.getId(), reported.getId(), ReportStatus.PENDING)) {
            throw new DuplicateResourceException("You already have a pending report for this user");
        }
        Report report = new Report();
        report.setReporter(reporter);
        report.setReported(reported);
        report.setReason(request.reason());
        report.setDescription(request.description());
        return socialMapper.toReport(reportRepository.save(report));
    }

    @Transactional(readOnly = true)
    public PageResponse<AdminReportResponse> listByStatus(ReportStatus status, Pageable pageable) {
        return PageResponse.from(reportRepository.findByStatus(status, pageable), socialMapper::toAdminReport);
    }

    @Transactional
    public AdminReportResponse review(Long reportId, ReviewReportRequest request) {
        if (request.status() == ReportStatus.PENDING) {
            throw new InvalidOperationException("A review must set status REVIEWED or DISMISSED");
        }
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("Report", reportId));
        report.setStatus(request.status());
        report.setResolutionNote(request.resolutionNote());
        report.setReviewedBy(currentUserService.getCurrentUser());
        report.setReviewedAt(clock.instant());
        if (request.suspendUser()) {
            report.getReported().setStatus(UserStatus.SUSPENDED);
        }
        return socialMapper.toAdminReport(report);
    }
}
