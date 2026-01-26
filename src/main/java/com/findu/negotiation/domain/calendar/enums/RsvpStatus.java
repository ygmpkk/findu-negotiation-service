package com.findu.negotiation.domain.calendar.enums;

import lombok.Getter;

/**
 * RSVP 响应状态枚举
 *
 * @author timothy
 * @date 2026/01/25
 */
@Getter
public enum RsvpStatus {
    /**
     * 待响应
     */
    PENDING(0),

    /**
     * 已接受
     */
    ACCEPTED(1),

    /**
     * 已拒绝
     */
    DECLINED(2),
    ;

    private final Integer code;

    RsvpStatus(Integer code) {
        this.code = code;
    }
}
