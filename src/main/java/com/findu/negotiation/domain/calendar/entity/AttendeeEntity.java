package com.findu.negotiation.domain.calendar.entity;

import com.findu.negotiation.domain.calendar.enums.AttendeeRole;
import com.findu.negotiation.domain.calendar.enums.RsvpStatus;
import lombok.*;

import java.util.Date;

/**
 * 日程参与人实体
 * <p>
 * Event 的参与客户实体（Customer）
 *
 * @author timothy
 * @date 2026/01/25
 */
@ToString
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendeeEntity {

    /**
     * 主键ID
     */
    private Long id;

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
    @NonNull
    private String customerId;

    /**
     * 参与角色
     * <p>
     * 可选值: REQUIRED（必需）, OPTIONAL（可选）
     *
     * @see com.findu.negotiation.domain.calendar.enums.AttendeeRole
     */
    @Builder.Default
    private Integer role = AttendeeRole.REQUIRED.getCode();

    /**
     * RSVP 响应状态
     * <p>
     * 可选值: PENDING（待响应）, ACCEPTED（已接受）, DECLINED（已拒绝）
     *
     * @see com.findu.negotiation.domain.calendar.enums.RsvpStatus
     */
    @Builder.Default
    private Integer rsvpStatus = RsvpStatus.PENDING.getCode();

    /**
     * 创建时间
     */
    private Date gmtCreate;

    /**
     * 判断是否为必需参与人
     */
    public boolean isRequired() {
        return AttendeeRole.REQUIRED.getCode().equals(role);
    }

    /**
     * 判断是否为可选参与人
     */
    public boolean isOptional() {
        return AttendeeRole.OPTIONAL.getCode().equals(role);
    }

    /**
     * 判断是否已接受
     */
    public boolean isAccepted() {
        return RsvpStatus.ACCEPTED.getCode().equals(rsvpStatus);
    }

    /**
     * 判断是否已拒绝
     */
    public boolean isDeclined() {
        return RsvpStatus.DECLINED.getCode().equals(rsvpStatus);
    }

    /**
     * 判断是否待响应
     */
    public boolean isPending() {
        return RsvpStatus.PENDING.getCode().equals(rsvpStatus);
    }
}
