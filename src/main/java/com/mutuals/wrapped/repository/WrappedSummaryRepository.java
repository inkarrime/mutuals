package com.mutuals.wrapped.repository;

import com.mutuals.wrapped.entity.WrappedPeriodType;
import com.mutuals.wrapped.entity.WrappedScope;
import com.mutuals.wrapped.entity.WrappedSummary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WrappedSummaryRepository extends JpaRepository<WrappedSummary, Long> {

    Optional<WrappedSummary> findByScopeAndUserIdAndPeriodTypeAndPeriodKey(WrappedScope scope, Long userId,
                                                                           WrappedPeriodType periodType, String periodKey);

    Optional<WrappedSummary> findByScopeAndUserIsNullAndPeriodTypeAndPeriodKey(WrappedScope scope,
                                                                               WrappedPeriodType periodType,
                                                                               String periodKey);

    List<WrappedSummary> findByUserIdAndScopeOrderByGeneratedAtDesc(Long userId, WrappedScope scope);
}
