package com.findu.negotiation.interfaces.dto.calendar;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.ToString;

/**
 * 更新日历请求
 *
 * @author timothy
 * @date 2026/01/25
 */
@ToString
@Data
public class UpdateCalendarRequest {

    /**
     * 日历ID
     */
    private String calendarId;

    /**
     * 日历名称
     */
    @Size(max = 128, message = "name长度不能超过128")
    private String name;

    /**
     * 日历描述
     */
    @Size(max = 512, message = "description长度不能超过512")
    private String description;

    /**
     * 时区
     */
    private String timezone;

    /**
     * 日历状态
     */
    private Integer status;
}
