package com.findu.negotiation.domain.calendar.enums;

import lombok.Getter;

/**
 * 日程类型枚举
 *
 * @author timothy
 * @date 2026/01/25
 */
@Getter
public enum EventType {
    /**
     * 普通日程 - 一次性发生的日程
     */
    SINGLE(0),

    /**
     * 重复性日程 - 按规则周期性发生的日程
     */
    RECURRING(1),
    ;

    private final Integer code;

    EventType(Integer code) {
        this.code = code;
    }
}