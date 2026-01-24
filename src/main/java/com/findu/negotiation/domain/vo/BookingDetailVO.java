package com.findu.negotiation.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class BookingDetailVO {
    private final TimeRangeVO timeRange;
    private final String location;
    private final List<String> attendeeIds;
}
