package com.findu.negotiation.interfaces.dto.calendar;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class CalendarCreateResponse {
    private String calendarId;
    private Map<String, Object> rawResponse;
}
