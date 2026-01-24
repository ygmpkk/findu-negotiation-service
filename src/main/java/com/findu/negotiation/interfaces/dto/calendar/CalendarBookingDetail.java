package com.findu.negotiation.interfaces.dto.calendar;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CalendarBookingDetail {
    private String location;
    private List<String> attendeeIds;
}
