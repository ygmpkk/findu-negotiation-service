package com.findu.negotiation.interfaces.dto.calendar;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder(toBuilder = true)
public class CalendarEventResponse {
    private String calendarId;
    private String eventId;
    private Map<String, Object> rawResponse;
}
