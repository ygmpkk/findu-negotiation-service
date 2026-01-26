package com.findu.negotiation.domain.calendar.entity;

import com.findu.negotiation.domain.calendar.enums.CalendarStatus;
import lombok.*;

import java.util.Date;
import java.util.List;

/**
 * 日历实体（聚合根）
 * <p>
 * Calendar 是包含一系列相关日程的容器，是同一类日程的实体。
 * 用于对同一业务语义下的 Event 进行管理。
 * <p>
 * 聚合根特性：
 * - Calendar 是 Event 的聚合根（Aggregate Root）
 * - Calendar 本身不表示时间占用，仅负责日程聚合
 * - 一个 Provider 可以拥有多个 Calendar
 * - Event 必须归属于一个 Calendar
 *
 * @author timothy
 * @date 2026/01/25
 */
@ToString
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CalendarEntity {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 日历唯一标识
     */
    private String calendarId;

    /**
     * 日历所有者（服务提供方）
     */
    private String providerId;

    /**
     * 日历名称
     */
    private String name;

    /**
     * 日历描述
     */
    private String description;

    /**
     * 默认时区
     * 例如：Asia/Shanghai
     */
    private String timezone;

    /**
     * 日历状态
     * <p>
     * 可选值: ACTIVE（激活）, DISABLED（禁用）
     *
     * @see com.findu.negotiation.domain.calendar.enums.CalendarStatus
     */
    @Builder.Default
    private Integer status = CalendarStatus.ACTIVE.getCode();

    /**
     * 创建时间
     */
    private Date gmtCreate;

    /**
     * 更新时间
     */
    private Date gmtModify;

    /**
     * 该日历包含的日程列表（仅在聚合查询时使用）
     * 注意：持久化时通常不包含此字段
     */
    @Builder.Default
    private List<EventEntity> eventEntities = List.of();

    /**
     * 判断是否为激活状态
     */
    public boolean isActive() {
        return CalendarStatus.ACTIVE.getCode().equals(status);
    }

    /**
     * 判断是否为禁用状态
     */
    public boolean isDisabled() {
        return CalendarStatus.DISABLED.getCode().equals(status);
    }
}
