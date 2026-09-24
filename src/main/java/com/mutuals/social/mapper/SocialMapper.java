package com.mutuals.social.mapper;

import com.mutuals.social.dto.AdminReportResponse;
import com.mutuals.social.dto.ReportResponse;
import com.mutuals.social.entity.Report;
import com.mutuals.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SocialMapper {

    private final UserMapper userMapper;

    public ReportResponse toReport(Report report) {
        return new ReportResponse(report.getId(), userMapper.toSummary(report.getReported()), report.getReason(),
                report.getDescription(), report.getStatus(), report.getCreatedAt());
    }

    public AdminReportResponse toAdminReport(Report report) {
        return new AdminReportResponse(
                report.getId(),
                userMapper.toSummary(report.getReporter()),
                userMapper.toSummary(report.getReported()),
                report.getReason(),
                report.getDescription(),
                report.getStatus(),
                report.getResolutionNote(),
                report.getCreatedAt(),
                report.getReviewedAt());
    }
}
