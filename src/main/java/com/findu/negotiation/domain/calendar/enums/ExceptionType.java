package com.findu.negotiation.domain.calendar.enums;

import lombok.Getter;

/**
 * @author timothy
 * @date 2026/1/25
 */
@Getter
public enum ExceptionType {
    DISABLED(0),
    ENABLED(1),
    ;

    private final Integer code;

    ExceptionType(Integer code) {
        this.code = code;
    }
}
