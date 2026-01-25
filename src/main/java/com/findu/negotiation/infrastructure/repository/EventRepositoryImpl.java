package com.findu.negotiation.infrastructure.repository;

import com.findu.negotiation.domain.calendar.repository.EventRepository;
import com.findu.negotiation.domain.calendar.entity.EventEntity;
import com.findu.negotiation.domain.calendar.enums.EventStatus;
import com.findu.negotiation.domain.calendar.enums.EventType;
import com.findu.negotiation.infrastructure.mapper.EventMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 日程仓储实现
 *
 * @author timothy
 * @date 2026/01/25
 */
@Slf4j
@Repository
public class EventRepositoryImpl implements EventRepository {
    @Autowired
    private EventMapper eventMapper;

    @Override
    public EventEntity save(EventEntity event) {
        // 先尝试更新，如果影响行数为0则插入
        int updated = eventMapper.update(event);
        if (updated == 0) {
            eventMapper.insertSelective(event);
        }
        return event;
    }

    @Override
    public List<EventEntity> saveAll(List<EventEntity> events) {
        if (events.isEmpty()) {
            return events;
        }
        eventMapper.insertBatch(events);
        return events;
    }

    @Override
    public Optional<EventEntity> findById(String eventId) {
        return eventMapper.findById(eventId);
    }

    @Override
    public List<EventEntity> findByCalendarId(String calendarId) {
        return eventMapper.findByCalendarId(calendarId);
    }

    @Override
    public List<EventEntity> findByCalendarIdAndStatus(String calendarId, EventStatus status) {
        return eventMapper.findByCalendarIdAndStatus(calendarId, status.getCode());
    }

    @Override
    public List<EventEntity> findByCalendarIdAndEventType(String calendarId, EventType eventType) {
        return eventMapper.findByCalendarIdAndEventType(calendarId, eventType.getCode());
    }

    @Override
    public List<EventEntity> findByTimeRange(String calendarId, LocalDateTime startTime, LocalDateTime endTime, String timezone) {
        return eventMapper.findByCalendarIdAndTimeRange(calendarId, startTime, endTime);
    }

    @Override
    public List<EventEntity> findScheduledByTimeRange(String calendarId, LocalDateTime startTime, LocalDateTime endTime, String timezone) {
        return eventMapper.findScheduledByCalendarIdAndTimeRange(calendarId, startTime, endTime);
    }

    @Override
    public List<EventEntity> findRecurringEvents(String calendarId) {
        return eventMapper.findRecurringEvents(calendarId);
    }

    @Override
    public List<EventEntity> findExceptionsByRecurringEventId(String recurringEventId) {
        return eventMapper.findExceptionsByRecurringEventId(recurringEventId);
    }

    @Override
    public boolean existsById(String eventId) {
        return eventMapper.existsById(eventId);
    }

    @Override
    public long countByCalendarId(String calendarId) {
        List<EventEntity> events = eventMapper.findByCalendarId(calendarId);
        return events.size();
    }

    @Override
    public void deleteById(String eventId) {
        eventMapper.deleteById(eventId);
    }

    @Override
    public void deleteAllById(List<String> eventIds) {
        eventMapper.deleteBatch(eventIds);
    }

    @Override
    public void deleteAllByCalendarId(String calendarId) {
        eventMapper.deleteAllByCalendarId(calendarId);
    }
}
