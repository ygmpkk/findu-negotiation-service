package com.findu.negotiation.domain.calendar.enums;

import lombok.Getter;

/**
 * 日程状态枚举
 *
 * @author timothy
 * @date 2026/01/25
 */
@Getter
public enum EventStatus {
    /**
     * 已安排
     */
    SCHEDULED(0),

    /**
     * 已取消
     */
    CANCELLED(1),

    /**
     * 已完成
     */
    FINISHED(2),
    ;

    private final Integer code;

    EventStatus(Integer code) {
        this.code = code;
    }
}