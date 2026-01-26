package com.findu.negotiation.domain.calendar.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.extern.jackson.Jacksonized;

/**
 * 提醒设置值对象
 * <p>
 * 支持三种提醒方式（互斥）：
 * - minutes: 提前N分钟提醒
 * - hour: 提前N小时提醒
 * - day: 提前N天提醒
 *
 * @author timothy
 * @date 2026/01/25
 */
@ToString
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Jacksonized
public class ReminderVO {

    /**
     * 提前提醒时间（单位：分钟）
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("minutes")
    private Integer minutes;

    /**
     * 提前提醒小时（单位：小时）
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("hour")
    private Integer hour;

    /**
     * 提前提醒天数（单位：天）
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("day")
    private Integer day;
}
