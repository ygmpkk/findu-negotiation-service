package com.findu.negotiation.domain.calendar;

import com.findu.negotiation.domain.calendar.entity.AttendeeEntity;
import com.findu.negotiation.domain.calendar.entity.CalendarEntity;
import com.findu.negotiation.domain.calendar.entity.EventEntity;
import com.findu.negotiation.domain.calendar.enums.*;
import com.findu.negotiation.domain.calendar.repository.AttendeeRepository;
import com.findu.negotiation.domain.calendar.repository.CalendarRepository;
import com.findu.negotiation.domain.calendar.repository.EventRepository;
import com.findu.negotiation.infrastructure.exception.BusinessException;
import com.findu.negotiation.infrastructure.exception.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.findu.negotiation.infrastructure.util.UUIDv7;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 日历领域服务实现
 *
 * @author timothy
 * @date 2026/01/25
 */
@Service
public class CalendarDomainServiceImpl implements CalendarDomainService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CalendarDomainServiceImpl.class);
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    @Autowired
    private CalendarRepository calendarRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private AttendeeRepository attendeeRepository;

    // ==================== Calendar 操作 ====================

    @Override
    @Transactional
    public CalendarEntity createCalendar(CalendarEntity calendarEntity) {
        LOGGER.info("Creating calendar: providerId={}, name={}", calendarEntity.getProviderId(), calendarEntity.getName());

        // 验证必填字段
        validateCalendar(calendarEntity);

        // 生成 ID
        if (calendarEntity.getCalendarId() == null || calendarEntity.getCalendarId().isEmpty()) {
            calendarEntity.setCalendarId(generateId());
        }

        // 设置默认状态
        if (calendarEntity.getStatus() == null) {
            calendarEntity.setStatus(CalendarStatus.ACTIVE.getCode());
        }

        // 保存
        CalendarEntity saved = calendarRepository.save(calendarEntity);
        LOGGER.info("Calendar created successfully: calendarId={}", saved.getCalendarId());
        return saved;
    }

    @Override
    @Transactional
    public CalendarEntity updateCalendar(CalendarEntity calendarEntity) {
        LOGGER.info("Updating calendar: calendarId={}", calendarEntity.getCalendarId());

        // 先查询现有数据
        CalendarEntity existing = calendarRepository.findById(calendarEntity.getCalendarId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "日历不存在: " + calendarEntity.getCalendarId()));

        // 合并更新字段（只更新非空字段）
        if (calendarEntity.getName() != null) {
            existing.setName(calendarEntity.getName());
        }
        if (calendarEntity.getDescription() != null) {
            existing.setDescription(calendarEntity.getDescription());
        }
        if (calendarEntity.getTimezone() != null) {
            existing.setTimezone(calendarEntity.getTimezone());
        }
        if (calendarEntity.getStatus() != null) {
            existing.setStatus(calendarEntity.getStatus());
        }

        // 验证必填字段
        validateCalendar(existing);

        // 保存
        CalendarEntity saved = calendarRepository.save(existing);
        LOGGER.info("Calendar updated successfully: calendarId={}", saved.getCalendarId());
        return saved;
    }

    @Override
    @Transactional
    public void deleteCalendar(String calendarId) {
        LOGGER.info("Deleting calendar: calendarId={}", calendarId);

        // 检查是否存在
        if (!calendarRepository.existsById(calendarId)) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "日历不存在: " + calendarId);
        }

        // 先删除该日历下的所有日程（应用层级联）
        List<EventEntity> events = eventRepository.findByCalendarId(calendarId);
        for (EventEntity event : events) {
            deleteEventCascade(event.getEventId());
        }

        // 删除日历
        calendarRepository.deleteById(calendarId);
        LOGGER.info("Calendar deleted successfully: calendarId={}", calendarId);
    }

    @Override
    public CalendarEntity getCalendar(String calendarId) {
        return calendarRepository.findById(calendarId).orElse(null);
    }

    @Override
    public List<CalendarEntity> getCalendarsByProvider(String providerId) {
        return calendarRepository.findByProviderId(providerId);
    }

    // ==================== Event 操作 ====================

    @Override
    @Transactional
    public EventEntity createEvent(EventEntity eventEntity) {
        LOGGER.info("Creating event: calendarId={}, title={}", eventEntity.getCalendarId(), eventEntity.getTitle());

        // 验证
        validateEvent(eventEntity);

        // 检查日历是否存在
        if (!calendarRepository.existsById(eventEntity.getCalendarId())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "日历不存在: " + eventEntity.getCalendarId());
        }

        // 冲突检测
        if (hasConflict(eventEntity.getCalendarId(), eventEntity)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "日程时间冲突");
        }

        // 生成 ID
        if (eventEntity.getEventId() == null || eventEntity.getEventId().isEmpty()) {
            eventEntity.setEventId(generateId());
        }

        // 设置默认状态
        if (eventEntity.getStatus() == null) {
            eventEntity.setStatus(EventStatus.SCHEDULED.getCode());
        }
        if (eventEntity.getFreeBusyStatus() == null) {
            eventEntity.setFreeBusyStatus(FreeBusyStatus.BUSY.getCode());
        }

        // 保存
        EventEntity saved = eventRepository.save(eventEntity);
        LOGGER.info("Event created successfully: eventId={}", saved.getEventId());
        return saved;
    }

    @Override
    @Transactional
    public EventEntity updateEvent(EventEntity eventEntity) {
        LOGGER.info("Updating event: eventId={}", eventEntity.getEventId());

        // 检查是否存在
        Optional<EventEntity> existing = eventRepository.findById(eventEntity.getEventId());
        if (existing.isEmpty()) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "日程不存在: " + eventEntity.getEventId());
        }

        // 验证
        validateEvent(eventEntity);

        // 冲突检测（排除自身）
        List<EventEntity> conflicts = findConflictingEvents(eventEntity.getCalendarId(), eventEntity);
        conflicts.removeIf(e -> e.getEventId().equals(eventEntity.getEventId()));
        if (!conflicts.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "日程时间冲突");
        }

        // 保存
        EventEntity saved = eventRepository.save(eventEntity);
        LOGGER.info("Event updated successfully: eventId={}", saved.getEventId());
        return saved;
    }

    @Override
    @Transactional
    public void deleteEvent(String eventId) {
        LOGGER.info("Deleting event: eventId={}", eventId);

        // 检查是否存在
        if (!eventRepository.existsById(eventId)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "日程不存在: " + eventId);
        }

        deleteEventCascade(eventId);
        LOGGER.info("Event deleted successfully: eventId={}", eventId);
    }

    @Override
    @Transactional
    public void cancelEvent(String eventId) {
        LOGGER.info("Cancelling event: eventId={}", eventId);

        Optional<EventEntity> optional = eventRepository.findById(eventId);
        if (optional.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "日程不存在: " + eventId);
        }

        EventEntity event = optional.get();
        event.setStatus(EventStatus.CANCELLED.getCode());
        eventRepository.save(event);
        LOGGER.info("Event cancelled successfully: eventId={}", eventId);
    }

    @Override
    public EventEntity getEvent(String eventId) {
        return eventRepository.findById(eventId).orElse(null);
    }

    @Override
    public List<EventEntity> getEventsByCalendar(String calendarId) {
        return eventRepository.findByCalendarId(calendarId);
    }

    @Override
    public List<EventEntity> getEventsInTimeRange(String calendarId, LocalDateTime startTime, LocalDateTime endTime, String timezone) {
        // 直接使用 LocalDateTime 进行查询
        return eventRepository.findByTimeRange(calendarId, startTime, endTime, timezone);
    }

    @Override
    public List<EventEntity> expandRecurringEvent(EventEntity recurringEventEntity, LocalDateTime rangeStart, LocalDateTime rangeEnd, String timezone) {
        if (!EventType.RECURRING.getCode().equals(recurringEventEntity.getEventType())) {
            return Collections.emptyList();
        }

        String recurrence = recurringEventEntity.getRecurrence();
        if (recurrence == null || recurrence.isEmpty()) {
            return Collections.emptyList();
        }

        List<EventEntity> expandedEvents = new ArrayList<>();
        LocalDateTime currentStart = recurringEventEntity.getStartTime();
        LocalDateTime currentEnd = recurringEventEntity.getEndTime();

        // 解析 RRULE 并展开
        Rrule rrule = parseRrule(recurrence);

        int count = 0;
        int maxCount = rrule.count != null ? rrule.count : 100; // 默认最多展开100个实例
        LocalDateTime until = rrule.until != null ? rrule.until : rangeEnd;

        while (currentStart.isBefore(until) && count < maxCount) {
            // 检查是否在查询范围内
            if (!currentStart.isBefore(rangeStart) && !currentEnd.isAfter(rangeEnd)) {
                EventEntity instance = createEventInstance(recurringEventEntity, currentStart, currentEnd, timezone);
                expandedEvents.add(instance);
            }
            count++;

            // 计算下一个实例
            currentStart = nextOccurrence(currentStart, rrule);
            currentEnd = nextOccurrence(currentEnd, rrule);
        }

        return expandedEvents;
    }

    @Override
    public boolean hasConflict(String calendarId, EventEntity eventEntity) {
        return !findConflictingEvents(calendarId, eventEntity).isEmpty();
    }

    @Override
    public List<TimeSlot> getFreeTimeSlots(String calendarId, LocalDateTime startTime, LocalDateTime endTime, String timezone) {
        // 创建查询时间范围
        TimeSlot queryRange = new TimeSlot(startTime, endTime);
        if (!queryRange.isValid()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "无效的时间范围");
        }

        // 获取指定时间范围内所有已安排的日程
        List<EventEntity> events = eventRepository.findScheduledByTimeRange(
                calendarId, startTime, endTime, timezone);

        List<TimeSlot> busySlots = new ArrayList<>();
        for (EventEntity event : events) {
            if (FreeBusyStatus.BUSY.getCode().equals(event.getFreeBusyStatus())) {
                TimeSlot eventSlot = new TimeSlot(event.getStartTime(), event.getEndTime());

                // 验证并添加有效的忙碌时间段
                if (eventSlot.isValid() && queryRange.overlaps(eventSlot)) {
                    busySlots.add(eventSlot);
                }
            }
        }

        // 按开始时间排序
        busySlots.sort((a, b) -> a.startTime().compareTo(b.startTime()));

        // 计算空闲时间段
        List<TimeSlot> freeSlots = new ArrayList<>();
        LocalDateTime current = startTime;

        for (TimeSlot busy : busySlots) {
            if (current.isBefore(busy.startTime())) {
                TimeSlot freeSlot = new TimeSlot(current, busy.startTime());
                if (freeSlot.isValid()) {
                    freeSlots.add(freeSlot);
                }
            }
            current = max(current, busy.endTime());
        }

        // 添加最后的空闲时间段
        if (current.isBefore(endTime)) {
            TimeSlot freeSlot = new TimeSlot(current, endTime);
            if (freeSlot.isValid()) {
                freeSlots.add(freeSlot);
            }
        }

        LOGGER.info("Found {} free time slots in calendarId={} between {} and {}", freeSlots.size(), calendarId, startTime, endTime);

        return freeSlots;
    }

    // ==================== Attendee 操作 ====================

    @Override
    @Transactional
    public AttendeeEntity addAttendee(String eventId, AttendeeEntity attendeeEntity) {
        LOGGER.info("Adding attendee: eventId={}, customerId={}", eventId, attendeeEntity.getCustomerId());

        // 检查日程是否存在
        if (!eventRepository.existsById(eventId)) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "日程不存在: " + eventId);
        }

        // 检查是否已存在
        if (attendeeRepository.existsByEventIdAndCustomerId(eventId, attendeeEntity.getCustomerId())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "该客户已是日程参与人");
        }

        // 设置关联
        attendeeEntity.setEventId(eventId);

        // 生成 ID
        if (attendeeEntity.getAttendeeId() == null || attendeeEntity.getAttendeeId().isEmpty()) {
            attendeeEntity.setAttendeeId(generateId());
        }

        // 设置默认值
        if (attendeeEntity.getRole() == null) {
            attendeeEntity.setRole(AttendeeRole.REQUIRED.getCode());
        }
        if (attendeeEntity.getRsvpStatus() == null) {
            attendeeEntity.setRsvpStatus(RsvpStatus.PENDING.getCode());
        }

        // 保存
        AttendeeEntity saved = attendeeRepository.save(attendeeEntity);
        LOGGER.info("Attendee added successfully: attendeeId={}", saved.getAttendeeId());
        return saved;
    }

    @Override
    @Transactional
    public void removeAttendee(String eventId, String attendeeId) {
        LOGGER.info("Removing attendee: attendeeId={}, eventId={}", attendeeId, eventId);

        // 检查是否存在
        if (!attendeeRepository.existsById(attendeeId)) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "参与人不存在: " + attendeeId);
        }

        attendeeRepository.deleteById(attendeeId);
        LOGGER.info("Attendee removed successfully: attendeeId={}", attendeeId);
    }

    @Override
    @Transactional
    public void updateRsvpStatus(String attendeeId, Integer status) {
        LOGGER.info("Updating RSVP status: attendeeId={}, status={}", attendeeId, status);

        Optional<AttendeeEntity> optional = attendeeRepository.findById(attendeeId);
        if (optional.isEmpty()) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "参与人不存在: " + attendeeId);
        }

        AttendeeEntity attendee = optional.get();
        attendee.setRsvpStatus(status);
        attendeeRepository.save(attendee);
        LOGGER.info("RSVP status updated successfully: attendeeId={}", attendeeId);
    }

    @Override
    public List<AttendeeEntity> getAttendees(String eventId) {
        return attendeeRepository.findByEventId(eventId);
    }

    // ==================== 私有辅助方法 ====================

    private void validateCalendar(CalendarEntity calendar) {
        if (calendar.getProviderId() == null || calendar.getProviderId().isEmpty()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "服务提供方ID不能为空");
        }
        if (calendar.getName() == null || calendar.getName().isEmpty()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "日历名称不能为空");
        }
    }

    private void validateEvent(EventEntity event) {
        if (event.getCalendarId() == null || event.getCalendarId().isEmpty()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "日历ID不能为空");
        }
        if (event.getTitle() == null || event.getTitle().isEmpty()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "日程标题不能为空");
        }
        if (event.getStartTime() == null || event.getEndTime() == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "日程时间不能为空");
        }
        if (event.getEventType() == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "日程类型不能为空");
        }
    }

    private void deleteEventCascade(String eventId) {
        // 先删除参与人
        attendeeRepository.deleteAllByEventId(eventId);
        // 再删除日程
        eventRepository.deleteById(eventId);
    }

    private String generateId() {
        return UUIDv7.generate();
    }

    private List<EventEntity> findConflictingEvents(String calendarId, EventEntity event) {
        LocalDateTime eventStart = event.getStartTime();
        LocalDateTime eventEnd = event.getEndTime();
        TimeSlot eventSlot = new TimeSlot(eventStart, eventEnd);

        // 获取时间范围内的所有日程
        List<EventEntity> eventsInRange = eventRepository.findByTimeRange(
                calendarId,
                eventStart.minusDays(7),  // 扩大范围以覆盖所有可能的冲突
                eventEnd.plusDays(7),
                event.getTimezone()
        );

        // 筛选出真正冲突的日程
        return eventsInRange.stream()
                .filter(e -> !EventStatus.CANCELLED.getCode().equals(e.getStatus()))
                .filter(e -> {
                    TimeSlot eSlot = new TimeSlot(e.getStartTime(), e.getEndTime());
                    return eventSlot.overlaps(eSlot);
                })
                .collect(Collectors.toList());
    }

    private LocalDateTime max(LocalDateTime a, LocalDateTime b) {
        return a.isAfter(b) ? a : b;
    }

    private EventEntity createEventInstance(EventEntity recurringEvent, LocalDateTime start, LocalDateTime end, String timezone) {
        return EventEntity.builder()
                .eventId(recurringEvent.getEventId() + "_" + start.format(DateTimeFormatter.ofPattern("yyyyMMddHHmm")))
                .calendarId(recurringEvent.getCalendarId())
                .title(recurringEvent.getTitle())
                .description(recurringEvent.getDescription())
                .visibility(recurringEvent.getVisibility())
                .startTime(start)
                .endTime(end)
                .timezone(timezone)
                .eventType(EventType.SINGLE.getCode())  // 实例作为普通日程
                .status(recurringEvent.getStatus())
                .freeBusyStatus(recurringEvent.getFreeBusyStatus())
                .isException(ExceptionType.DISABLED.getCode())
                .recurringEventId(recurringEvent.getEventId())
                .reminderVOS(recurringEvent.getReminderVOS())
                .locationVO(recurringEvent.getLocationVO())
                .build();
    }

    /**
     * 简单的 RRULE 解析器
     * 支持常见模式：
     * - FREQ=DAILY;COUNT=n
     * - FREQ=WEEKLY;BYDAY=MO,TU,WE,TH,FR,SA,SU;COUNT=n
     * - FREQ=MONTHLY;BYMONTHDAY=n;COUNT=n
     * - FREQ=YEARLY;BYMONTH=n;BYMONTHDAY=n;COUNT=n
     */
    private Rrule parseRrule(String recurrence) {
        Rrule rrule = new Rrule();
        String[] parts = recurrence.split(";");

        for (String part : parts) {
            String[] kv = part.split("=");
            if (kv.length != 2) continue;

            String key = kv[0].trim();
            String value = kv[1].trim();

            switch (key) {
                case "FREQ" -> rrule.freq = value;
                case "COUNT" -> rrule.count = Integer.parseInt(value);
                case "UNTIL" -> rrule.until = LocalDateTime.parse(value, TIMESTAMP_FORMATTER);
                case "INTERVAL" -> rrule.interval = Integer.parseInt(value);
                case "BYDAY" -> rrule.byDay = parseByDay(value);
                case "BYMONTHDAY" -> rrule.byMonthDay = Integer.parseInt(value);
                case "BYMONTH" -> rrule.byMonth = Integer.parseInt(value);
            }
        }

        return rrule;
    }

    private List<DayOfWeek> parseByDay(String byDay) {
        return Stream.of(byDay.split(","))
                .map(this::mapToDayOfWeek)
                .collect(Collectors.toList());
    }

    private DayOfWeek mapToDayOfWeek(String day) {
        return switch (day) {
            case "MO" -> DayOfWeek.MONDAY;
            case "TU" -> DayOfWeek.TUESDAY;
            case "WE" -> DayOfWeek.WEDNESDAY;
            case "TH" -> DayOfWeek.THURSDAY;
            case "FR" -> DayOfWeek.FRIDAY;
            case "SA" -> DayOfWeek.SATURDAY;
            case "SU" -> DayOfWeek.SUNDAY;
            default -> throw new IllegalArgumentException("Unknown day: " + day);
        };
    }

    private LocalDateTime nextOccurrence(LocalDateTime current, Rrule rrule) {
        int interval = rrule.interval != null ? rrule.interval : 1;

        return switch (rrule.freq) {
            case "MINUTE" -> current.plusMinutes(interval);
            case "HOUR" -> current.plusHours(interval);
            case "DAILY" -> current.plusDays(interval);
            case "WEEKLY" -> {
                if (rrule.byDay == null || rrule.byDay.isEmpty()) {
                    yield current.plusWeeks(interval);
                }
                // 找下一个匹配的工作日
                LocalDateTime next = current.plusDays(1);
                while (!rrule.byDay.contains(next.getDayOfWeek())) {
                    next = next.plusDays(1);
                }
                yield next;
            }
            case "MONTHLY" -> {
                if (rrule.byMonthDay != null) {
                    yield current.plusMonths(interval).withDayOfMonth(Math.min(rrule.byMonthDay, current.toLocalDate().lengthOfMonth()));
                }
                yield current.plusMonths(interval);
            }
            case "YEARLY" -> current.plusYears(interval);
            default -> throw new IllegalArgumentException("Unknown frequency: " + rrule.freq);
        };
    }

    /**
     * RRULE 解析结果
     */
    private static class Rrule {
        String freq;              // MINUTE, HOUR, DAILY, WEEKLY, MONTHLY, YEARLY
        Integer interval;         // 间隔，默认1
        Integer count;            // 重复次数
        LocalDateTime until;      // 结束日期
        List<DayOfWeek> byDay;    // BYDAY (MO, TU, WE, TH, FR, SA, SU)
        Integer byMonthDay;       // BYMONTHDAY (1-31)
        Integer byMonth;          // BYMONTH (1-12)
    }
}
