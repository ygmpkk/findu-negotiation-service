package com.findu.negotiation.domain.calendar.entity;

import com.findu.negotiation.domain.calendar.enums.*;
import com.findu.negotiation.domain.calendar.vo.LocationVO;
import com.findu.negotiation.domain.calendar.vo.ReminderVO;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

/**
 * 日程实体
 * <p>
 * Event 是会议预定与预约系统中的基本实体，表示一个确定的日期或时间范围。
 * Event 表示时间占用的最小业务单元。
 *
 * @author timothy
 * @date 2026/01/25
 */
@ToString
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventEntity {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 日程唯一标识
     */
    private String eventId;

    /**
     * 所属日历ID
     */
    private String calendarId;

    /**
     * 日程标题
     */
    private String title;

    /**
     * 日程描述
     */
    private String description;

    /**
     * 日程可见性
     * <p>
     * 可选值: PUBLIC（公开）, PRIVATE（私有）
     *
     * @see com.findu.negotiation.domain.calendar.enums.Visibility
     */
    @Builder.Default
    private Integer visibility = Visibility.PRIVATE.getCode();

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;

    /**
     * 时区
     * <p>
     * 例如: Asia/Shanghai, America/New_York
     */
    @Builder.Default
    private String timezone = "Asia/Shanghai";

    /**
     * 日程类型
     * <p>
     * 可选值: SINGLE（普通日程）, RECURRING（重复日程）
     *
     * @see com.findu.negotiation.domain.calendar.enums.EventType
     */
    @NonNull
    private Integer eventType = EventType.SINGLE.getCode();

    /**
     * 重复日程的重复性规则
     * <p>
     * 使用 RFC5545 中的 RRULE 定义。
     * 非重复日程或例外日程的该字段为空。
     * <p>
     * 示例值：
     * - "FREQ=DAILY;COUNT=10" - 每天重复，共10次
     * - "FREQ=WEEKLY;BYDAY=MO,WE,FR" - 每周一、三、五重复
     * - "FREQ=MONTHLY;BYMONTHDAY=15" - 每月15日重复
     */
    private String recurrence;

    /**
     * 日程状态
     * <p>
     * 可选值: SCHEDULED（已安排）, CANCELLED（已取消）, FINISHED（已完成）
     *
     * @see com.findu.negotiation.domain.calendar.enums.EventStatus
     */
    @Builder.Default
    private Integer status = EventStatus.SCHEDULED.getCode();

    /**
     * 闲忙状态
     * <p>
     * 可选值: FREE（空闲）, BUSY（忙碌）
     *
     * @see com.findu.negotiation.domain.calendar.enums.FreeBusyStatus
     */
    @Builder.Default
    private Integer freeBusyStatus = FreeBusyStatus.BUSY.getCode();

    /**
     * 是否为重复日程的例外实例
     * <p>
     * @see ExceptionType
     */
    @Builder.Default
    private Integer isException = ExceptionType.DISABLED.getCode();

    /**
     * 如果是例外日程，指向其所属的重复日程的 eventId
     */
    private String recurringEventId;

    /**
     * 提前提醒设置列表（单位：分钟）
     * <p>
     * 示例值：[15, 30] 表示提前15分钟和30分钟各提醒一次
     */
    @Builder.Default
    private List<ReminderVO> reminderVOS = List.of();

    /**
     * 位置信息
     */
    private LocationVO locationVO;

    /**
     * 创建时间
     */
    private Date gmtCreate;

    /**
     * 更新时间
     */
    private Date gmtModify;

    /**
     * 该日程包含的参与人列表（仅在聚合查询时使用）
     * 注意：持久化时通常不包含此字段
     */
    @Builder.Default
    private List<AttendeeEntity> attendeeEntities = List.of();

    /**
     * 判断是否为重复日程
     */
    public boolean isRecurring() {
        return EventType.RECURRING.getCode().equals(eventType);
    }

    /**
     * 判断是否为例外日程
     * @return
     */
    public boolean isException() {
        return ExceptionType.ENABLED.getCode().equals(isException);
    }

    /**
     * 判断是否为普通日程
     */
    public boolean isSingle() {
        return EventType.SINGLE.getCode().equals(eventType);
    }

    /**
     * 判断是否已被取消
     */
    public boolean isCancelled() {
        return EventStatus.CANCELLED.getCode().equals(status);
    }

    /**
     * 判断是否已完成
     */
    public boolean isFinished() {
        return EventStatus.FINISHED.getCode().equals(status);
    }

    /**
     * 判断是否已安排
     */
    public boolean isScheduled() {
        return EventStatus.SCHEDULED.getCode().equals(status);
    }

    /**
     * 判断是否为空闲状态
     */
    public boolean isFree() {
        return FreeBusyStatus.FREE.getCode().equals(freeBusyStatus);
    }

    /**
     * 判断是否为忙碌状态
     */
    public boolean isBusy() {
        return FreeBusyStatus.BUSY.getCode().equals(freeBusyStatus);
    }

    /**
     * 判断是否为公开日程
     */
    public boolean isPublic() {
        return Visibility.PUBLIC.getCode().equals(visibility);
    }

    /**
     * 判断是否为私有日程
     */
    public boolean isPrivate() {
        return Visibility.PRIVATE.getCode().equals(visibility);
    }
}
