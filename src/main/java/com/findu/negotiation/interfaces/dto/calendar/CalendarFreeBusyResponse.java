package com.findu.negotiation.interfaces.dto.calendar;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class CalendarFreeBusyResponse {
    private String providerId;
    private List<CalendarBusySlot> busySlots;
    private Map<String, Object> rawResponse;
}
