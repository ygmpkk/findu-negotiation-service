package com.findu.negotiation.interfaces.dto.calendar;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CalendarAppointmentRequest {
    @NotBlank(message = "providerId不能为空")
    private String providerId;

    @NotBlank(message = "customerId不能为空")
    private String customerId;

    @NotBlank(message = "demandId不能为空")
    private String demandId;

    private String calendarId;

    @NotBlank(message = "summary不能为空")
    private String summary;

    private String description;

    @NotBlank(message = "startTime不能为空")
    private String startTime;

    @NotBlank(message = "endTime不能为空")
    private String endTime;

    private String timezone;

    private String userIdType;
}
