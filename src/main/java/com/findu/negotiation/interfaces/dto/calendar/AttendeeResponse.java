package com.findu.negotiation.interfaces.dto.calendar;

import com.findu.negotiation.domain.calendar.entity.AttendeeEntity;
import lombok.*;

import java.util.Date;
import java.util.List;

/**
 * 参与人响应
 *
 * @author timothy
 * @date 2026/01/25
 */
@ToString
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendeeResponse {

    /**
     * 参与人唯一标识
     */
    private String attendeeId;

    /**
     * 所属日程ID
     */
    private String eventId;

    /**
     * 参与人客户ID
     */
    private String customerId;

    /**
     * 参与角色
     */
    private Integer role;

    /**
     * RSVP 响应状态
     */
    private Integer rsvpStatus;

    /**
     * 创建时间
     */
    private Date gmtCreate;

    /**
     * 从实体转换
     */
    public static AttendeeResponse fromEntity(AttendeeEntity entity) {
        return AttendeeResponse.builder()
                .attendeeId(entity.getAttendeeId())
                .eventId(entity.getEventId())
                .customerId(entity.getCustomerId())
                .role(entity.getRole())
                .rsvpStatus(entity.getRsvpStatus())
                .gmtCreate(entity.getGmtCreate())
                .build();
    }

    /**
     * 从实体列表转换
     */
    public static List<AttendeeResponse> fromEntities(List<AttendeeEntity> entities) {
        return entities.stream()
                .map(AttendeeResponse::fromEntity)
                .collect(java.util.stream.Collectors.toList());
    }
}
