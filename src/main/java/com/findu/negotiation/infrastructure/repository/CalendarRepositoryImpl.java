package com.findu.negotiation.infrastructure.repository;

import com.findu.negotiation.domain.calendar.repository.CalendarRepository;
import com.findu.negotiation.domain.calendar.entity.CalendarEntity;
import com.findu.negotiation.domain.calendar.enums.CalendarStatus;
import com.findu.negotiation.infrastructure.mapper.CalendarMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 日历仓储实现
 *
 * @author timothy
 * @date 2026/01/25
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class CalendarRepositoryImpl implements CalendarRepository {

    @Autowired
    private CalendarMapper calendarMapper;

    @Override
    public CalendarEntity save(CalendarEntity calendar) {
        // 先尝试更新，如果影响行数为0则插入
        int updated = calendarMapper.update(calendar);
        if (updated == 0) {
            calendarMapper.insertSelective(calendar);
        }
        return calendar;
    }

    @Override
    public List<CalendarEntity> saveAll(List<CalendarEntity> calendars) {
        if (calendars.isEmpty()) {
            return calendars;
        }
        calendarMapper.insertBatch(calendars);
        return calendars;
    }

    @Override
    public Optional<CalendarEntity> findById(String calendarId) {
        return calendarMapper.findById(calendarId);
    }

    @Override
    public List<CalendarEntity> findByProviderId(String providerId) {
        return calendarMapper.findByProviderId(providerId);
    }

    @Override
    public List<CalendarEntity> findByProviderIdAndStatus(String providerId, CalendarStatus status) {
        return calendarMapper.findByProviderIdAndStatus(providerId, status.getCode());
    }

    @Override
    public List<CalendarEntity> findAll() {
        log.warn("findAll() not implemented in CalendarMapper, returning empty list");
        return List.of();
    }

    @Override
    public boolean existsById(String calendarId) {
        return calendarMapper.existsById(calendarId);
    }

    @Override
    public long countByProviderId(String providerId) {
        List<CalendarEntity> calendars = calendarMapper.findByProviderId(providerId);
        return calendars.size();
    }

    @Override
    public void deleteById(String calendarId) {
        calendarMapper.deleteById(calendarId);
    }

    @Override
    public void deleteAllById(List<String> calendarIds) {
        calendarIds.forEach(this::deleteById);
    }

    @Override
    public void deleteAllByProviderId(String providerId) {
        List<CalendarEntity> calendars = calendarMapper.findByProviderId(providerId);
        calendars.forEach(calendar -> deleteById(calendar.getCalendarId()));
    }
}
