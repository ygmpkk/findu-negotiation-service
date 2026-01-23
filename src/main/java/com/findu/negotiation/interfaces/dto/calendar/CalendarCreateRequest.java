package com.findu.negotiation.interfaces.dto.calendar;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CalendarCreateRequest {
    @NotBlank(message = "summary不能为空")
    private String summary;

    private String description;
}
