package com.findu.negotiation.application;

import com.findu.negotiation.domain.calendar.entity.AttendeeEntity;
import com.findu.negotiation.domain.calendar.entity.CalendarEntity;
import com.findu.negotiation.domain.calendar.entity.EventEntity;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 日历业务服务接口
 * <p>
 * 提供日历相关的业务操作，封装领域服务和外部服务调用
 *
 * @author timothy
 * @date 2026/01/25
 */
public interface CalendarBizService {

    // ==================== Calendar 业务操作 ====================

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

    /**
     * 为服务提供方创建默认日历
     *
     * @param providerId 服务提供方ID
     * @param name       日历名称
     * @return 创建的日历
     */
    CalendarEntity createDefaultCalendar(String providerId, String name);

    /**
     * 获取服务提供方的激活日历列表
     *
     * @param providerId 服务提供方ID
     * @return 激活的日历列表
     */
    List<CalendarEntity> getActiveCalendars(String providerId);

    /**
     * 获取闲忙状态
     * <p>
     * 返回指定时间范围内的忙碌时间段（不显示具体日程详情）
     *
     * @param providerId 服务提供方ID
     * @param startTime  开始时间
     * @param endTime    结束时间
     * @param timezone   时区
     * @return 闲忙状态列表（每个日历一个）
     */
    List<FreeBusy> getFreeBusyStatus(String providerId, LocalDateTime startTime, LocalDateTime endTime, String timezone);

    // ==================== Event 业务操作 ====================

    /**
     * 创建日程
     *
     * @param eventEntity 日程实体
     * @return 创建后的日程
     */
    EventEntity createEvent(EventEntity eventEntity);

    /**
     * 更新日程
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
     * 创建服务预约日程
     * <p>
     * 为服务预约场景创建日程，自动设置默认值和提醒
     *
     * @param calendarId 日历ID
     * @param title      日程标题
     * @param startTime  开始时间
     * @param endTime    结束时间
     * @param timezone   时区
     * @param customerId 客户ID
     * @return 创建的日程
     */
    EventEntity createAppointment(String calendarId, String title, LocalDateTime startTime, LocalDateTime endTime, String timezone, String customerId);

    /**
     * 批量创建预约日程（用于创建重复预约）
     *
     * @param appointments 预约日程列表
     * @return 创建的日程列表
     */
    List<EventEntity> createAppointments(List<AppointmentRequest> appointments);

    /**
     * 获取日历在指定时间范围内的空闲时间段
     *
     * @param calendarId 日历ID
     * @param startTime  开始时间
     * @param endTime    结束时间
     * @param timezone   时区
     * @param duration   预约时长（分钟）
     * @return 可用的空闲时间段列表
     */
    List<TimeSlot> getAvailableSlots(String calendarId, LocalDateTime startTime, LocalDateTime endTime, String timezone, int duration);

    /**
     * 检查并预约时间段
     * <p>
     * 检查指定时间段是否可用，如果可用则创建预约
     *
     * @param calendarId 日历ID
     * @param title      预约标题
     * @param startTime  开始时间
     * @param endTime    结束时间
     * @param timezone   时区
     * @param customerId 客户ID
     * @return 创建的预约，如果时间冲突返回 null
     */
    EventEntity checkAndBook(String calendarId, String title, LocalDateTime startTime, LocalDateTime endTime, String timezone, String customerId);

    /**
     * 客户确认预约
     *
     * @param eventId    日程ID
     * @param customerId 客户ID
     * @return 更新后的日程
     */
    EventEntity confirmAppointment(String eventId, String customerId);

    /**
     * 客户取消预约
     *
     * @param eventId    日程ID
     * @param customerId 客户ID
     * @return 是否取消成功
     */
    boolean cancelAppointment(String eventId, String customerId);

    /**
     * 获取客户的预约列表
     *
     * @param customerId 客户ID
     * @param startTime  开始时间（可选）
     * @param endTime    结束时间（可选）
     * @return 预约列表
     */
    List<EventEntity> getCustomerAppointments(String customerId, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 获取服务提供方的预约列表
     *
     * @param providerId 服务提供方ID
     * @param startTime  开始时间（可选）
     * @param endTime    结束时间（可选）
     * @return 预约列表
     */
    List<EventEntity> getProviderAppointments(String providerId, LocalDateTime startTime, LocalDateTime endTime);

    // ==================== Attendee 业务操作 ====================

    /**
     * 添加参与人
     *
     * @param eventId      日程ID
     * @param attendeeEntity 参与人实体
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
     * @param status     RSVP 状态
     */
    void updateRsvpStatus(String attendeeId, Integer status);

    /**
     * 获取日程的所有参与人
     *
     * @param eventId 日程ID
     * @return 参与人列表
     */
    List<AttendeeEntity> getAttendees(String eventId);

    /**
     * 根据 ID 获取参与人
     *
     * @param attendeeId 参与人ID
     * @return 参与人实体
     */
    AttendeeEntity getAttendee(String attendeeId);

    // ==================== 提醒操作 ====================

    /**
     * 获取需要发送提醒的日程列表
     *
     * @param minutesAhead 提前多少分钟
     * @return 需要提醒的日程列表
     */
    List<EventEntity> getEventsNeedReminder(int minutesAhead);

    /**
     * 获取日程的提醒接收人列表
     *
     * @param eventId 日程ID
     * @return 提醒接收人ID列表
     */
    List<String> getReminderRecipients(String eventId);

    // ==================== 内部类 ====================

    /**
     * 预约请求
     */
    record AppointmentRequest(
            String calendarId,
            String title,
            LocalDateTime startTime,
            LocalDateTime endTime,
            String timezone,
            String customerId
    ) {
    }

    /**
     * 时间段
     */
    record TimeSlot(LocalDateTime startTime, LocalDateTime endTime) {
    }

    /**
     * 闲忙状态
     */
    record FreeBusy(
            String calendarId,
            String providerId,
            String timezone,
            LocalDateTime startTime,
            LocalDateTime endTime,
            List<BusyTimeSlot> busyTimes
    ) {
    }

    /**
     * 忙碌时间段
     */
    record BusyTimeSlot(
            LocalDateTime startTime,
            LocalDateTime endTime,
            String status
    ) {
    }
}
