package com.findu.negotiation.interfaces.dto.calendar;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CalendarFreeBusyRequest {
    @NotBlank(message = "providerId不能为空")
    private String providerId;

    @NotBlank(message = "startTime不能为空")
    private String startTime;

    @NotBlank(message = "endTime不能为空")
    private String endTime;

    private String timezone;
    private String userIdType;
}
