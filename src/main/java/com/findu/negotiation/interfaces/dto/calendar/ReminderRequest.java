package com.findu.negotiation.interfaces.dto.calendar;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;

/**
 * 提醒设置请求
 *
 * @author timothy
 * @date 2026/01/25
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Jacksonized
public class ReminderRequest {

    /**
     * 提前提醒时间（单位：分钟）
     */
    @JsonProperty("minutes")
    private Integer minutes;

    /**
     * 提前提醒小时（单位：小时）
     */
    @JsonProperty("hour")
    private Integer hour;

    /**
     * 提前提醒天数（单位：天）
     */
    @JsonProperty("day")
    private Integer day;
}
