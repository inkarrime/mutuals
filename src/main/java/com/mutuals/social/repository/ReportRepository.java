package com.mutuals.social.repository;

import com.mutuals.social.entity.Report;
import com.mutuals.social.entity.ReportStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportRepository extends JpaRepository<Report, Long> {

    Page<Report> findByStatus(ReportStatus status, Pageable pageable);

    boolean existsByReporterIdAndReportedIdAndStatus(Long reporterId, Long reportedId, ReportStatus status);

    long countByStatus(ReportStatus status);
}
