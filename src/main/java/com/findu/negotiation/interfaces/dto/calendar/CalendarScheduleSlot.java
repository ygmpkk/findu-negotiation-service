package com.findu.negotiation.interfaces.dto.calendar;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CalendarScheduleSlot {
    private String startTime;
    private String endTime;
    private String status;
    private Integer priceCents;
    private CalendarBookingDetail bookingDetail;
}
