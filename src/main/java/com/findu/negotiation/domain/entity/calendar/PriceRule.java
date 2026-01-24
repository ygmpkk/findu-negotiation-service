package com.findu.negotiation.domain.entity.calendar;

import lombok.Builder;
import lombok.Data;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;

/**
 * 价格规则
 */
@Data
@Builder
public class PriceRule {
    private Set<DayOfWeek> daysOfWeek;
    private LocalTime startTime;
    private LocalTime endTime;
    private LocalDate startDate;
    private LocalDate endDate;
    private long priceInCents;

    public boolean matches(CalendarTimeRange range) {
        if (range == null || range.getStart() == null || range.getEnd() == null) {
            return false;
        }
        LocalDate slotDate = range.getStart().toLocalDate();
        if (startDate != null && slotDate.isBefore(startDate)) {
            return false;
        }
        if (endDate != null && slotDate.isAfter(endDate)) {
            return false;
        }
        if (daysOfWeek != null && !daysOfWeek.isEmpty()
                && !daysOfWeek.contains(range.getStart().getDayOfWeek())) {
            return false;
        }
        LocalTime slotStart = range.getStart().toLocalTime();
        LocalTime slotEnd = range.getEnd().toLocalTime();
        if (startTime != null && slotStart.isBefore(startTime)) {
            return false;
        }
        if (endTime != null && slotEnd.isAfter(endTime)) {
            return false;
        }
        return true;
    }
}
