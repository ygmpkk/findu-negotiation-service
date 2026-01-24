package com.findu.negotiation.interfaces.dto.calendar;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class CalendarScheduleRequest {
    @NotBlank(message = "providerId不能为空")
    private String providerId;

    @NotBlank(message = "startTime不能为空")
    private String startTime;

    @NotBlank(message = "endTime不能为空")
    private String endTime;

    private String timezone;

    private String userIdType;

    private String calendarId;

    private ViewerRole viewerRole = ViewerRole.STUDENT;

    @Valid
    private List<TimeRangeRequest> workingRanges;

    @Valid
    private List<PricingRuleRequest> pricingRules;

    public enum ViewerRole {
        COACH,
        STUDENT
    }
}
