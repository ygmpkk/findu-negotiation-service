package com.findu.negotiation.domain.calendar.enums;

import lombok.Getter;

/**
 * 闲忙状态枚举
 *
 * @author timothy
 * @date 2026/01/25
 */
@Getter
public enum FreeBusyStatus {
    /**
     * 空闲 - 可被预约
     */
    FREE(0),

    /**
     * 忙碌 - 已被占用
     */
    BUSY(1),
    ;

    private final Integer code;

    FreeBusyStatus(Integer code) {
        this.code = code;
    }
}
