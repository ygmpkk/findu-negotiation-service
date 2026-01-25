package com.findu.negotiation.domain.calendar;

import com.findu.negotiation.domain.calendar.entity.AttendeeEntity;
import com.findu.negotiation.domain.calendar.entity.CalendarEntity;
import com.findu.negotiation.domain.calendar.entity.EventEntity;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 日历领域服务接口
 * <p>
 * 定义日历相关的核心领域操作
 *
 * @author timothy
 * @date 2026/01/25
 */
public interface CalendarDomainService {

    // ==================== Calendar 操作 ====================

    /**
     * 创建日历
     *
     * @param calendarEntity 日历实体
     * @return 创建后的日历
     */
    CalendarEntity createCalendar(CalendarEntity calendarEntity);

    /**
     * 更新日历
     *
     * @param calendarEntity 日历实体
     * @return 更新后的日历
     */
    CalendarEntity updateCalendar(CalendarEntity calendarEntity);

    /**
     * 删除日历
     * <p>
     * 注意：删除日历时需要处理其包含的 Event
     *
     * @param calendarId 日历ID
     */
    void deleteCalendar(String calendarId);

    /**
     * 根据 ID 获取日历
     *
     * @param calendarId 日历ID
     * @return 日历实体，不存在返回 null
     */
    CalendarEntity getCalendar(String calendarId);

    /**
     * 获取服务提供方的所有日历
     *
     * @param providerId 服务提供方ID
     * @return 日历列表
     */
    List<CalendarEntity> getCalendarsByProvider(String providerId);

    // ==================== Event 操作 ====================

    /**
     * 创建日程
     * <p>
     * 需要进行冲突检测
     *
     * @param eventEntity 日程实体
     * @return 创建后的日程
     */
    EventEntity createEvent(EventEntity eventEntity);

    /**
     * 更新日程
     * <p>
     * 需要进行冲突检测
     *
     * @param eventEntity 日程实体
     * @return 更新后的日程
     */
    EventEntity updateEvent(EventEntity eventEntity);

    /**
     * 删除日程
     *
     * @param eventId 日程ID
     */
    void deleteEvent(String eventId);

    /**
     * 取消日程
     *
     * @param eventId 日程ID
     */
    void cancelEvent(String eventId);

    /**
     * 根据 ID 获取日程
     *
     * @param eventId 日程ID
     * @return 日程实体，不存在返回 null
     */
    EventEntity getEvent(String eventId);

    /**
     * 获取日历中的所有日程
     *
     * @param calendarId 日历ID
     * @return 日程列表
     */
    List<EventEntity> getEventsByCalendar(String calendarId);

    /**
     * 获取指定时间范围内的日程
     *
     * @param calendarId  日历ID
     * @param startTime   开始时间
     * @param endTime     结束时间
     * @param timezone    时区
     * @return 日程列表
     */
    List<EventEntity> getEventsInTimeRange(String calendarId, LocalDateTime startTime, LocalDateTime endTime, String timezone);

    /**
     * 展开重复日程的实例
     * <p>
     * 将重复日程按照规则展开为指定时间范围内的实际日程实例
     *
     * @param recurringEventEntity 重复日程
     * @param rangeStart     时间范围开始
     * @param rangeEnd       时间范围结束
     * @param timezone       时区
     * @return 展开后的日程实例列表
     */
    List<EventEntity> expandRecurringEvent(EventEntity recurringEventEntity, LocalDateTime rangeStart, LocalDateTime rangeEnd, String timezone);

    /**
     * 检测日程冲突
     * <p>
     * 检测给定日程是否与日历中已有日程冲突
     *
     * @param calendarId 日历ID
     * @param eventEntity      待检测的日程
     * @return 是否存在冲突
     */
    boolean hasConflict(String calendarId, EventEntity eventEntity);

    /**
     * 获取空闲时间段
     * <p>
     * 计算指定时间范围内的空闲时间段
     *
     * @param calendarId 日历ID
     * @param startTime  开始时间
     * @param endTime    结束时间
     * @param timezone   时区
     * @return 空闲时间段列表
     */
    List<TimeSlot> getFreeTimeSlots(String calendarId, LocalDateTime startTime, LocalDateTime endTime, String timezone);

    // ==================== Attendee 操作 ====================

    /**
     * 添加参与人
     *
     * @param eventId  日程ID
     * @param attendeeEntity 参与人
     * @return 添加后的参与人
     */
    AttendeeEntity addAttendee(String eventId, AttendeeEntity attendeeEntity);

    /**
     * 移除参与人
     *
     * @param eventId    日程ID
     * @param attendeeId 参与人ID
     */
    void removeAttendee(String eventId, String attendeeId);

    /**
     * 更新参与人 RSVP 状态
     *
     * @param attendeeId 参与人ID
     * @param status     RSVP 状态 (PENDING, ACCEPTED, DECLINED)
     */
    void updateRsvpStatus(String attendeeId, Integer status);

    /**
     * 获取日程的所有参与人
     *
     * @param eventId 日程ID
     * @return 参与人列表
     */
    List<AttendeeEntity> getAttendees(String eventId);

    // ==================== 内部类 ====================

    /**
     * 时间段值对象
     */
    record TimeSlot(LocalDateTime startTime, LocalDateTime endTime) {

        /**
         * 验证时间段是否有效（开始时间早于结束时间）
         */
        public boolean isValid() {
            return startTime != null && endTime != null && startTime.isBefore(endTime);
        }

        /**
         * 计算持续时间（分钟）
         */
        public long durationMinutes() {
            if (!isValid()) {
                return 0;
            }
            return java.time.Duration.between(startTime, endTime).toMinutes();
        }

        /**
         * 计算持续时间（小时）
         */
        public double durationHours() {
            return durationMinutes() / 60.0;
        }

        /**
         * 判断是否包含指定时间点
         */
        public boolean contains(LocalDateTime dateTime) {
            if (dateTime == null || !isValid()) {
                return false;
            }
            return !dateTime.isBefore(startTime) && dateTime.isBefore(endTime);
        }

        /**
         * 判断是否与另一个时间段重叠
         */
        public boolean overlaps(TimeSlot other) {
            if (other == null || !isValid() || !other.isValid()) {
                return false;
            }
            return startTime.isBefore(other.endTime) && endTime.isAfter(other.startTime);
        }

        /**
         * 判断是否完全包含另一个时间段
         */
        public boolean contains(TimeSlot other) {
            if (other == null || !isValid() || !other.isValid()) {
                return false;
            }
            return !startTime.isAfter(other.startTime) && !endTime.isBefore(other.endTime);
        }

        /**
         * 判断是否与另一个时间段相邻（可以合并）
         */
        public boolean isAdjacentTo(TimeSlot other) {
            if (other == null || !isValid() || !other.isValid()) {
                return false;
            }
            return endTime.equals(other.startTime) || startTime.equals(other.endTime);
        }

        /**
         * 合并相邻或重叠的时间段
         */
        public TimeSlot merge(TimeSlot other) {
            if (other == null || !overlaps(other) && !isAdjacentTo(other)) {
                return this;
            }
            LocalDateTime newStart = startTime.isBefore(other.startTime) ? startTime : other.startTime;
            LocalDateTime newEnd = endTime.isAfter(other.endTime) ? endTime : other.endTime;
            return new TimeSlot(newStart, newEnd);
        }

        /**
         * 获取与另一个时间段的交集
         */
        public TimeSlot intersection(TimeSlot other) {
            if (other == null || !overlaps(other)) {
                return null;
            }
            LocalDateTime newStart = startTime.isAfter(other.startTime) ? startTime : other.startTime;
            LocalDateTime newEnd = endTime.isBefore(other.endTime) ? endTime : other.endTime;
            return new TimeSlot(newStart, newEnd);
        }

        @Override
        public String toString() {
            return String.format("[%s - %s]", startTime, endTime);
        }
    }
}
