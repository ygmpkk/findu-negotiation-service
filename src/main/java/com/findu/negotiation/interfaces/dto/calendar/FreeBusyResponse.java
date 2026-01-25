package com.findu.negotiation.interfaces.dto.calendar;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 闲忙状态响应
 *
 * @author timothy
 * @date 2026/01/25
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FreeBusyResponse {

    /**
     * 日历ID
     */
    private String calendarId;

    /**
     * 服务提供方ID
     */
    private String providerId;

    /**
     * 时区
     */
    private String timezone;

    /**
     * 查询时间范围开始
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime startTime;

    /**
     * 查询时间范围结束
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime endTime;

    /**
     * 忙碌时间段列表
     */
    private List<BusyTimeSlot> busyTimes;

    /**
     * 忙碌时间段
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BusyTimeSlot {

        /**
         * 开始时间
         */
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime startTime;

        /**
         * 结束时间
         */
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime endTime;

        /**
         * 状态描述
         */
        private String status;
    }
}
