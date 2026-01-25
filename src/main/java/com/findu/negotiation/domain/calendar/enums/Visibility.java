package com.findu.negotiation.domain.calendar.enums;

import lombok.Getter;

/**
 * 日程可见性枚举
 *
 * @author timothy
 * @date 2026/01/25
 */
@Getter
public enum Visibility {
    /**
     * 公开 - 所有人可见
     */
    PUBLIC(0),

    /**
     * 私有 - 仅本人可见
     */
    PRIVATE(1),
    ;

    private final Integer code;

    Visibility(Integer code) {
        this.code = code;
    }
}
