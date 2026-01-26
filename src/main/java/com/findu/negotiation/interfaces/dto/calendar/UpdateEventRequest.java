package com.findu.negotiation.interfaces.dto.calendar;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * 更新日程请求
 *
 * @author timothy
 * @date 2026/01/25
 */
@ToString
@Data
public class UpdateEventRequest {

    /**
     * 日程ID
     */
    @NotBlank(message = "eventId不能为空")
    private String eventId;

    /**
     * 日程标题
     */
    @Size(max = 256, message = "title长度不能超过256")
    private String title;

    /**
     * 日程描述
     */
    private String description;

    /**
     * 可见性
     */
    private Integer visibility;

    /**
     * 开始时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime endTime;

    /**
     * 时区
     */
    private String timezone;

    /**
     * 日程状态
     */
    private Integer status;

    /**
     * 闲忙状态
     */
    private Integer freeBusyStatus;

    /**
     * 提前提醒设置列表
     */
    private java.util.List<ReminderRequest> reminders;

    /**
     * 位置信息
     */
    private LocationRequest location;
}
