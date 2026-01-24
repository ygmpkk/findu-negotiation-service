package com.findu.negotiation.domain.entity.calendar;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 日历时间段
 */
@Data
@Builder
public class CalendarTimeRange {
    private LocalDateTime start;
    private LocalDateTime end;

    public boolean overlaps(CalendarTimeRange other) {
        if (other == null || other.start == null || other.end == null || start == null || end == null) {
            return false;
        }
        return start.isBefore(other.end) && end.isAfter(other.start);
    }

    public boolean contains(LocalDateTime time) {
        if (time == null || start == null || end == null) {
            return false;
        }
        return !time.isBefore(start) && time.isBefore(end);
    }
}
