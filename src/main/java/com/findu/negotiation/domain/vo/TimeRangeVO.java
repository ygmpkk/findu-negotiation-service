package com.findu.negotiation.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;

@Getter
@AllArgsConstructor
public class TimeRangeVO {
    private final Instant start;
    private final Instant end;

    public boolean overlaps(TimeRangeVO other) {
        return start.isBefore(other.end) && end.isAfter(other.start);
    }

    public TimeRangeVO intersect(TimeRangeVO other) {
        Instant maxStart = start.isAfter(other.start) ? start : other.start;
        Instant minEnd = end.isBefore(other.end) ? end : other.end;
        if (maxStart.isBefore(minEnd)) {
            return new TimeRangeVO(maxStart, minEnd);
        }
        return null;
    }
}
