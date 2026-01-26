package com.findu.negotiation.interfaces.dto.calendar;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.ToString;

/**
 * 创建参与人请求
 *
 * @author timothy
 * @date 2026/01/25
 */
@ToString
@Data
public class CreateAttendeeRequest {

    /**
     * 日程ID
     */
    @NotBlank(message = "eventId不能为空")
    private String eventId;

    /**
     * 参与人客户ID
     */
    @NotBlank(message = "customerId不能为空")
    private String customerId;

    /**
     * 参与角色 (0: 必需, 1: 可选)
     */
    private Integer role;

    /**
     * RSVP 响应状态 (0: 待响应, 1: 已接受, 2: 已拒绝)
     */
    private Integer rsvpStatus;
}
