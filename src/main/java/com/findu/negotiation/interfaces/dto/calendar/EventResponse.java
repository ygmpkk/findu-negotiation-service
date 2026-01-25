package com.findu.negotiation.interfaces.dto.calendar;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.findu.negotiation.domain.calendar.entity.EventEntity;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

/**
 * 日程响应
 *
 * @author timothy
 * @date 2026/01/25
 */
@ToString
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventResponse {

    /**
     * 日程ID
     */
    private String eventId;

    /**
     * 所属日历ID
     */
    private String calendarId;

    /**
     * 日程标题
     */
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
     * 日程类型
     */
    private Integer eventType;

    /**
     * 重复规则
     */
    private String recurrence;

    /**
     * 日程状态
     */
    private Integer status;

    /**
     * 闲忙状态
     */
    private Integer freeBusyStatus;

    /**
     * 是否为重复日程的例外实例
     */
    private Integer isException;

    /**
     * 重复日程ID
     */
    private String recurringEventId;

    /**
     * 提醒设置列表
     */
    private List<Object> reminders;

    /**
     * 位置信息
     */
    private Object location;

    /**
     * 创建时间
     */
    private Date gmtCreate;

    /**
     * 更新时间
     */
    private Date gmtModify;

    /**
     * 从实体转换
     */
    public static EventResponse fromEntity(EventEntity entity) {
        return EventResponse.builder()
                .eventId(entity.getEventId())
                .calendarId(entity.getCalendarId())
                .title(entity.getTitle())
                .description(entity.getDescription())
                .visibility(entity.getVisibility())
                .startTime(entity.getStartTime())
                .endTime(entity.getEndTime())
                .timezone(entity.getTimezone())
                .eventType(entity.getEventType())
                .recurrence(entity.getRecurrence())
                .status(entity.getStatus())
                .freeBusyStatus(entity.getFreeBusyStatus())
                .isException(entity.getIsException())
                .recurringEventId(entity.getRecurringEventId())
                .reminders(entity.getReminderVOS() != null ? entity.getReminderVOS().stream().map(r -> (Object) r).collect(java.util.stream.Collectors.toList()) : List.of())
                .location(entity.getLocationVO())
                .gmtCreate(entity.getGmtCreate())
                .gmtModify(entity.getGmtModify())
                .build();
    }

    /**
     * 从实体列表转换
     */
    public static List<EventResponse> fromEntities(List<EventEntity> entities) {
        return entities.stream()
                .map(EventResponse::fromEntity)
                .collect(java.util.stream.Collectors.toList());
    }
}
