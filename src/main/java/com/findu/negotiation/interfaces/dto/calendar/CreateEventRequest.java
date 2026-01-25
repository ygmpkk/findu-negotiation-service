package com.findu.negotiation.interfaces.dto.calendar;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * 创建日程请求
 *
 * @author timothy
 * @date 2026/01/25
 */
@ToString
@Data
public class CreateEventRequest {

    /**
     * 日历ID
     */
    @NotBlank(message = "calendarId不能为空")
    private String calendarId;

    /**
     * 日程标题
     */
    @NotBlank(message = "title不能为空")
    @Size(max = 256, message = "title长度不能超过256")
    private String title;

    /**
     * 日程描述
     */
    private String description;

    /**
     * 可见性 (0: 公开, 1: 私有)
     */
    private Integer visibility;

    /**
     * 开始时间
     */
    @NotNull(message = "startTime不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    @NotNull(message = "endTime不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime endTime;

    /**
     * 时区
     */
    private String timezone;

    /**
     * 日程类型 (0: 普通日程, 1: 重复日程)
     */
    @NotNull(message = "eventType不能为空")
    private Integer eventType;

    /**
     * 重复规则 (RFC5545 RRULE)
     * 仅当 eventType=1 时需要
     */
    private String recurrence;

    /**
     * 提前提醒设置列表（单位：分钟）
     */
    private java.util.List<ReminderRequest> reminders;

    /**
     * 位置信息
     */
    private LocationRequest location;
}
