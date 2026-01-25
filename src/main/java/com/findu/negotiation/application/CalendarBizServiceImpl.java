package com.findu.negotiation.application;

import com.alibaba.fastjson.JSON;
import com.findu.negotiation.domain.calendar.CalendarDomainService;
import com.findu.negotiation.domain.calendar.entity.AttendeeEntity;
import com.findu.negotiation.domain.calendar.entity.CalendarEntity;
import com.findu.negotiation.domain.calendar.entity.EventEntity;
import com.findu.negotiation.domain.calendar.enums.*;
import com.findu.negotiation.domain.calendar.vo.ReminderVO;
import com.findu.negotiation.infrastructure.exception.BusinessException;
import com.findu.negotiation.infrastructure.exception.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 日历业务服务实现
 *
 * @author timothy
 * @date 2026/01/25
 */
@Service
public class CalendarBizServiceImpl implements CalendarBizService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CalendarBizServiceImpl.class);

    // 默认预约提前提醒时间（分钟）
    private static final int DEFAULT_REMINDER_MINUTES = 30;

    @Autowired
    private CalendarDomainService calendarDomainService;

    // ==================== Calendar 业务操作 ====================

    @Override
    @Transactional
    public CalendarEntity createDefaultCalendar(String providerId, String name) {
        LOGGER.info("Creating default calendar for provider: providerId={}, name={}", providerId, name);

        // 检查该 providerId 是否已创建日历
        List<CalendarEntity> existingCalendars = calendarDomainService.getCalendarsByProvider(providerId);
        if (!existingCalendars.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "该服务提供方已创建日历，每个 providerId 只能创建一个日历");
        }

        CalendarEntity calendar = CalendarEntity.builder()
                .providerId(providerId)
                .name(name)
                .description("默认日历")
                .timezone("Asia/Shanghai")
                .status(CalendarStatus.ACTIVE.getCode())
                .build();

        return calendarDomainService.createCalendar(calendar);
    }

    @Override
    public List<CalendarEntity> getActiveCalendars(String providerId) {
        List<CalendarEntity> allCalendars = calendarDomainService.getCalendarsByProvider(providerId);
        return allCalendars.stream()
                .filter(c -> CalendarStatus.ACTIVE.getCode().equals(c.getStatus()))
                .collect(Collectors.toList());
    }

    @Override
    public List<FreeBusy> getFreeBusyStatus(String providerId, LocalDateTime startTime, LocalDateTime endTime, String timezone) {
        LOGGER.info("Getting free/busy status: providerId={}, startTime={}, endTime={}, timezone={}",
                providerId, startTime, endTime, timezone);

        // 获取激活的日历列表
        List<CalendarEntity> calendars = getActiveCalendars(providerId);
        List<FreeBusy> result = new ArrayList<>();

        for (CalendarEntity calendar : calendars) {
            // 获取该日历在指定时间范围内的忙碌日程
            List<com.findu.negotiation.domain.calendar.entity.EventEntity> busyEvents =
                    calendarDomainService.getEventsInTimeRange(calendar.getCalendarId(), startTime, endTime, timezone);

            // 提取忙碌时间段并合并相邻/重叠的时间段
            List<BusyTimeSlot> busyTimes = extractAndMergeBusyTimes(busyEvents, startTime, endTime);

            FreeBusy status = new FreeBusy(
                    calendar.getCalendarId(),
                    providerId,
                    timezone,
                    startTime,
                    endTime,
                    busyTimes
            );
            result.add(status);
        }

        return result;
    }

    /**
     * 从忙碌日程中提取并合并相邻/重叠的忙碌时间段
     *
     * @param events    忙碌日程列表
     * @param rangeStart 查询范围开始时间
     * @param rangeEnd   查询范围结束时间
     * @return 合并后的忙碌时间段列表
     */
    private List<BusyTimeSlot> extractAndMergeBusyTimes(
            List<com.findu.negotiation.domain.calendar.entity.EventEntity> events,
            LocalDateTime rangeStart,
            LocalDateTime rangeEnd) {

        List<BusyTimeSlot> busyTimes = new ArrayList<>();

        // 筛选出忙碌状态的已安排日程
        List<LocalDateTime[]> ranges = events.stream()
                .filter(e -> com.findu.negotiation.domain.calendar.enums.FreeBusyStatus.BUSY.getCode()
                        .equals(e.getFreeBusyStatus()))
                .filter(e -> com.findu.negotiation.domain.calendar.enums.EventStatus.SCHEDULED.getCode()
                        .equals(e.getStatus()))
                .map(e -> new LocalDateTime[]{e.getStartTime(), e.getEndTime()})
                .sorted((a, b) -> a[0].compareTo(b[0]))
                .collect(Collectors.toList());

        // 合并相邻或重叠的时间段
        for (LocalDateTime[] range : ranges) {
            LocalDateTime start = range[0];
            LocalDateTime end = range[1];

            // 限制在查询范围内
            if (end.isBefore(rangeStart) || start.isAfter(rangeEnd)) {
                continue;
            }
            start = start.isBefore(rangeStart) ? rangeStart : start;
            end = end.isAfter(rangeEnd) ? rangeEnd : end;

            // 检查是否与最后一个时间段相邻或重叠
            if (!busyTimes.isEmpty()) {
                BusyTimeSlot last = busyTimes.get(busyTimes.size() - 1);
                // 如果相邻或重叠，合并它们
                if (!start.isAfter(last.endTime())) {
                    LocalDateTime mergedEnd = last.endTime().isAfter(end) ? last.endTime() : end;
                    busyTimes.set(busyTimes.size() - 1,
                            new BusyTimeSlot(last.startTime(), mergedEnd, "busy"));
                    continue;
                }
            }

            // 添加新的忙碌时间段
            busyTimes.add(new BusyTimeSlot(start, end, "busy"));
        }

        return busyTimes;
    }

    @Override
    @Transactional
    public CalendarEntity createCalendar(CalendarEntity calendarEntity) {
        // 检查该 providerId 是否已创建日历
        List<CalendarEntity> existingCalendars = calendarDomainService.getCalendarsByProvider(calendarEntity.getProviderId());
        if (!existingCalendars.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "该服务提供方已创建日历，每个 providerId 只能创建一个日历");
        }

        return calendarDomainService.createCalendar(calendarEntity);
    }

    @Override
    @Transactional
    public CalendarEntity updateCalendar(CalendarEntity calendarEntity) {
        return calendarDomainService.updateCalendar(calendarEntity);
    }

    @Override
    @Transactional
    public void deleteCalendar(String calendarId) {
        calendarDomainService.deleteCalendar(calendarId);
    }

    @Override
    public CalendarEntity getCalendar(String calendarId) {
        return calendarDomainService.getCalendar(calendarId);
    }

    @Override
    public List<CalendarEntity> getCalendarsByProvider(String providerId) {
        return calendarDomainService.getCalendarsByProvider(providerId);
    }

    // ==================== Event 业务操作 ====================

    @Override
    @Transactional
    public EventEntity createEvent(EventEntity eventEntity) {
        return calendarDomainService.createEvent(eventEntity);
    }

    @Override
    @Transactional
    public EventEntity updateEvent(EventEntity eventEntity) {
        return calendarDomainService.updateEvent(eventEntity);
    }

    @Override
    @Transactional
    public void deleteEvent(String eventId) {
        calendarDomainService.deleteEvent(eventId);
    }

    @Override
    public EventEntity getEvent(String eventId) {
        return calendarDomainService.getEvent(eventId);
    }

    @Override
    public List<EventEntity> getEventsByCalendar(String calendarId) {
        return calendarDomainService.getEventsByCalendar(calendarId);
    }

    @Override
    @Transactional
    public EventEntity createAppointment(String calendarId, String title, LocalDateTime startTime, LocalDateTime endTime, String timezone, String customerId) {
        LOGGER.info("Creating appointment: calendarId={}, title={}, customerId={}", calendarId, title, customerId);

        // 检查日历是否存在
        CalendarEntity calendar = calendarDomainService.getCalendar(calendarId);
        if (calendar == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "日历不存在: " + calendarId);
        }

        // 创建提醒
        ReminderVO reminder = ReminderVO.builder()
                .minutes(DEFAULT_REMINDER_MINUTES)
                .hour(0)
                .day(0)
                .build();

        // 创建日程
        EventEntity event = EventEntity.builder()
                .calendarId(calendarId)
                .title(title)
                .description("服务预约")
                .visibility(Visibility.PRIVATE.getCode())
                .startTime(startTime)
                .endTime(endTime)
                .timezone(timezone != null ? timezone : "Asia/Shanghai")
                .eventType(EventType.SINGLE.getCode())
                .status(EventStatus.SCHEDULED.getCode())
                .freeBusyStatus(FreeBusyStatus.BUSY.getCode())
                .reminderVOS(List.of(reminder))
                .build();

        EventEntity created = calendarDomainService.createEvent(event);

        // 添加客户作为参与人
        if (customerId != null && !customerId.isEmpty()) {
            try {
                AttendeeEntity attendee = AttendeeEntity.builder()
                        .customerId(customerId)
                        .role(AttendeeRole.REQUIRED.getCode())
                        .rsvpStatus(RsvpStatus.PENDING.getCode())
                        .build();
                calendarDomainService.addAttendee(created.getEventId(), attendee);
            } catch (Exception e) {
                LOGGER.warn("Failed to add attendee: {}", e.getMessage());
            }
        }

        return created;
    }

    @Override
    @Transactional
    public List<EventEntity> createAppointments(List<AppointmentRequest> appointments) {
        List<EventEntity> results = new ArrayList<>();
        for (AppointmentRequest request : appointments) {
            try {
                EventEntity event = createAppointment(
                        request.calendarId(),
                        request.title(),
                        request.startTime(),
                        request.endTime(),
                        request.timezone(),
                        request.customerId()
                );
                results.add(event);
            } catch (Exception e) {
                LOGGER.error("Failed to create appointment: {}", e.getMessage(), e);
            }
        }
        return results;
    }

    @Override
    public List<TimeSlot> getAvailableSlots(String calendarId, LocalDateTime startTime, LocalDateTime endTime, String timezone, int duration) {
        // 获取空闲时间段
        List<CalendarDomainService.TimeSlot> freeSlots = calendarDomainService.getFreeTimeSlots(
                calendarId, startTime, endTime, timezone);

        // 根据预约时长筛选可用的空闲时间段
        List<TimeSlot> availableSlots = new ArrayList<>();
        for (CalendarDomainService.TimeSlot slot : freeSlots) {
            long slotDuration = java.time.Duration.between(slot.startTime(), slot.endTime()).toMinutes();
            if (slotDuration >= duration) {
                // 将时间段切分为多个可预约的时间段
                LocalDateTime current = slot.startTime();
                while (current.plusMinutes(duration).isBefore(slot.endTime()) || current.plusMinutes(duration).equals(slot.endTime())) {
                    availableSlots.add(new TimeSlot(current, current.plusMinutes(duration)));
                    current = current.plusMinutes(duration);
                }
            }
        }

        return availableSlots;
    }

    @Override
    @Transactional
    public EventEntity checkAndBook(String calendarId, String title, LocalDateTime startTime, LocalDateTime endTime, String timezone, String customerId) {
        // 先检查是否有冲突
        EventEntity checkEvent = EventEntity.builder()
                .calendarId(calendarId)
                .title(title)
                .startTime(startTime)
                .endTime(endTime)
                .timezone(timezone != null ? timezone : "Asia/Shanghai")
                .eventType(EventType.SINGLE.getCode())
                .build();

        if (calendarDomainService.hasConflict(calendarId, checkEvent)) {
            return null;
        }

        // 无冲突，创建预约
        return createAppointment(calendarId, title, startTime, endTime, timezone, customerId);
    }

    @Override
    @Transactional
    public EventEntity confirmAppointment(String eventId, String customerId) {
        LOGGER.info("Confirming appointment: eventId={}, customerId={}", eventId, customerId);

        EventEntity event = calendarDomainService.getEvent(eventId);
        if (event == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "日程不存在: " + eventId);
        }

        // 更新参与人 RSVP 状态为已接受
        List<AttendeeEntity> attendees = calendarDomainService.getAttendees(eventId);
        for (AttendeeEntity attendee : attendees) {
            if (customerId.equals(attendee.getCustomerId())) {
                calendarDomainService.updateRsvpStatus(attendee.getAttendeeId(), RsvpStatus.ACCEPTED.getCode());
                break;
            }
        }

        // 更新日程状态为已安排
        event.setStatus(EventStatus.SCHEDULED.getCode());
        return calendarDomainService.updateEvent(event);
    }

    @Override
    @Transactional
    public boolean cancelAppointment(String eventId, String customerId) {
        LOGGER.info("Cancelling appointment: eventId={}, customerId={}", eventId, customerId);

        EventEntity event = calendarDomainService.getEvent(eventId);
        if (event == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "日程不存在: " + eventId);
        }

        // 检查客户是否是该预约的参与人
        List<AttendeeEntity> attendees = calendarDomainService.getAttendees(eventId);
        boolean isAttendee = attendees.stream()
                .anyMatch(a -> customerId.equals(a.getCustomerId()));

        if (!isAttendee) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "该客户不是此预约的参与人");
        }

        // 更新参与人 RSVP 状态为已拒绝
        for (AttendeeEntity attendee : attendees) {
            if (customerId.equals(attendee.getCustomerId())) {
                calendarDomainService.updateRsvpStatus(attendee.getAttendeeId(), RsvpStatus.DECLINED.getCode());
                break;
            }
        }

        // 取消日程
        calendarDomainService.cancelEvent(eventId);
        return true;
    }

    @Override
    public List<EventEntity> getCustomerAppointments(String customerId, LocalDateTime startTime, LocalDateTime endTime) {
        // 获取客户作为参与人的所有日程
        // 注意：这里需要通过 AttendeeRepository 查询，暂时返回空列表
        // 实际实现需要注入 AttendeeRepository
        LOGGER.warn("getCustomerAppointments not fully implemented, requires AttendeeRepository");
        return new ArrayList<>();
    }

    @Override
    public List<EventEntity> getProviderAppointments(String providerId, LocalDateTime startTime, LocalDateTime endTime) {
        // 获取服务提供方的所有日历
        List<CalendarEntity> calendars = getActiveCalendars(providerId);

        List<EventEntity> allAppointments = new ArrayList<>();
        for (CalendarEntity calendar : calendars) {
            List<EventEntity> events = calendarDomainService.getEventsInTimeRange(
                    calendar.getCalendarId(), startTime, endTime, "Asia/Shanghai");
            allAppointments.addAll(events);
        }

        // 按开始时间排序
        allAppointments.sort(Comparator.comparing(EventEntity::getStartTime));
        return allAppointments;
    }

    // ==================== Attendee 业务操作 ====================

    @Override
    @Transactional
    public AttendeeEntity addAttendee(String eventId, AttendeeEntity attendeeEntity) {
        return calendarDomainService.addAttendee(eventId, attendeeEntity);
    }

    @Override
    @Transactional
    public void removeAttendee(String eventId, String attendeeId) {
        calendarDomainService.removeAttendee(eventId, attendeeId);
    }

    @Override
    @Transactional
    public void updateRsvpStatus(String attendeeId, Integer status) {
        calendarDomainService.updateRsvpStatus(attendeeId, status);
    }

    @Override
    public List<AttendeeEntity> getAttendees(String eventId) {
        return calendarDomainService.getAttendees(eventId);
    }

    @Override
    public AttendeeEntity getAttendee(String attendeeId) {
        // 注意：CalendarDomainService 没有直接提供 getAttendee 方法
        // 需要通过 repository 获取，这里暂时返回 null
        // 实际实现需要注入 AttendeeRepository
        LOGGER.warn("getAttendee not fully implemented, requires AttendeeRepository");
        return null;
    }

    // ==================== 提醒操作 ====================

    @Override
    public List<EventEntity> getEventsNeedReminder(int minutesAhead) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime reminderTime = now.plusMinutes(minutesAhead);

        List<EventEntity> eventsNeedReminder = new ArrayList<>();

        // 获取所有激活日历
        // 注意：这里需要更高效的实现，暂时遍历所有日历
        // 实际实现应该使用专门的查询方法

        return eventsNeedReminder;
    }

    @Override
    public List<String> getReminderRecipients(String eventId) {
        List<AttendeeEntity> attendees = calendarDomainService.getAttendees(eventId);
        return attendees.stream()
                .map(AttendeeEntity::getCustomerId)
                .collect(Collectors.toList());
    }

    // ==================== 私有辅助方法 ====================
}
