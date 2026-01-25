package com.findu.negotiation.domain.calendar.enums;

import lombok.Getter;

/**
 * 日历状态枚举
 *
 * @author timothy
 * @date 2026/01/25
 */
@Getter
public enum CalendarStatus {
    /**
     * 禁用状态
     */
    DISABLED(0),
    /**
     * 激活状态
     */
    ACTIVE(1),
    ;

    private final Integer code;

    CalendarStatus(Integer code) {
        this.code = code;
    }
}