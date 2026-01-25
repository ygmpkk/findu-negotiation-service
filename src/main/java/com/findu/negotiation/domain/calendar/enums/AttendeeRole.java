package com.findu.negotiation.domain.calendar.enums;

import lombok.Getter;

/**
 * 参与人角色枚举
 *
 * @author timothy
 * @date 2026/01/25
 */
@Getter
public enum AttendeeRole {
    /**
     * 必需参与人
     */
    REQUIRED(0),

    /**
     * 可选参与人
     */
    OPTIONAL(1),
    ;

    private final Integer code;

    AttendeeRole(int code) {
        this.code = code;
    }
}
