package com.findu.negotiation.interfaces.dto.calendar;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.ToString;

/**
 * 更新参与人请求
 *
 * @author timothy
 * @date 2026/01/25
 */
@ToString
@Data
public class UpdateAttendeeRequest {

    /**
     * 参与人ID
     */
    @NotBlank(message = "attendeeId不能为空")
    private String attendeeId;

    /**
     * 参与角色 (0: 必需, 1: 可选)
     */
    private Integer role;

    /**
     * RSVP 响应状态 (0: 待响应, 1: 已接受, 2: 已拒绝)
     */
    private Integer rsvpStatus;
}
