package com.findu.negotiation.interfaces.dto.calendar;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CalendarScheduleResponse {
    private String providerId;
    private String timezone;
    private List<CalendarScheduleSlot> slots;
}
