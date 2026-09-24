package com.mutuals.wrapped.dto;

import com.mutuals.wrapped.entity.WrappedPeriodType;

import java.time.Instant;
import java.time.LocalDate;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZoneId;

public record WrappedPeriod(WrappedPeriodType type, String key, LocalDate from, LocalDate to) {

    public static WrappedPeriod month(YearMonth month) {
        return new WrappedPeriod(WrappedPeriodType.MONTHLY, month.toString(), month.atDay(1), month.atEndOfMonth());
    }

    public static WrappedPeriod year(Year year) {
        return new WrappedPeriod(WrappedPeriodType.YEARLY, year.toString(), year.atDay(1), year.atMonth(12).atEndOfMonth());
    }

    public static WrappedPeriod allTime(LocalDate today) {
        return new WrappedPeriod(WrappedPeriodType.ALL_TIME, "ALL", LocalDate.of(2020, 1, 1), today);
    }

    public static WrappedPeriod parse(String key) {
        return key.length() == 4 ? year(Year.parse(key)) : month(YearMonth.parse(key));
    }

    public Instant fromInstant(ZoneId zone) {
        return from.atStartOfDay(zone).toInstant();
    }

    public Instant toInstant(ZoneId zone) {
        return to.plusDays(1).atStartOfDay(zone).toInstant().minusMillis(1);
    }
}
