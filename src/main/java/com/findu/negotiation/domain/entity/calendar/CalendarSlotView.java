package com.findu.negotiation.domain.entity.calendar;

import lombok.Builder;
import lombok.Data;

/**
 * 日历时段展示
 */
@Data
@Builder
public class CalendarSlotView {
    private CalendarTimeRange timeRange;
    private SlotAvailability availability;
    private Long priceInCents;
    private String bookingId;
    private String bookedById;
    private String bookedByName;
    private String locationTitle;
}
