package com.findu.negotiation.interfaces.dto.calendar;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CalendarBusySlot {
    private String startTime;
    private String endTime;
}
