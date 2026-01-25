package com.findu.negotiation.interfaces.dto.calendar;

import com.findu.negotiation.domain.calendar.entity.CalendarEntity;
import lombok.*;
import lombok.extern.jackson.Jacksonized;

import java.util.Date;
import java.util.List;

/**
 * 日历响应
 *
 * @author timothy
 * @date 2026/01/25
 */
@ToString
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Jacksonized
public class CalendarResponse {

    /**
     * 日历ID
     */
    private String calendarId;

    /**
     * 服务提供方ID
     */
    private String providerId;

    /**
     * 日历名称
     */
    private String name;

    /**
     * 日历描述
     */
    private String description;

    /**
     * 时区
     */
    private String timezone;

    /**
     * 日历状态
     */
    private Integer status;

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
    public static CalendarResponse fromEntity(CalendarEntity entity) {
        return CalendarResponse.builder()
                .calendarId(entity.getCalendarId())
                .providerId(entity.getProviderId())
                .name(entity.getName())
                .description(entity.getDescription())
                .timezone(entity.getTimezone())
                .status(entity.getStatus())
                .gmtCreate(entity.getGmtCreate())
                .gmtModify(entity.getGmtModify())
                .build();
    }

    /**
     * 从实体列表转换
     */
    public static List<CalendarResponse> fromEntities(java.util.List<CalendarEntity> entities) {
        return entities.stream()
                .map(CalendarResponse::fromEntity)
                .collect(java.util.stream.Collectors.toList());
    }
}
