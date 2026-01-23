package com.findu.negotiation.interfaces.dto.calendar;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CalendarEventUpdateRequest {
    @NotBlank(message = "providerId不能为空")
    private String providerId;

    private String calendarId;

    @NotBlank(message = "summary不能为空")
    private String summary;

    private String description;

    @NotBlank(message = "startTime不能为空")
    private String startTime;

    @NotBlank(message = "endTime不能为空")
    private String endTime;

    private String timezone;

    private List<String> attendeeIds;

    private String userIdType;
}
