package com.findu.negotiation.infrastructure.repository;

import com.findu.negotiation.domain.calendar.repository.AttendeeRepository;
import com.findu.negotiation.domain.calendar.entity.AttendeeEntity;
import com.findu.negotiation.domain.calendar.enums.AttendeeRole;
import com.findu.negotiation.domain.calendar.enums.RsvpStatus;
import com.findu.negotiation.infrastructure.mapper.AttendeeMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 参与人仓储实现
 *
 * @author timothy
 * @date 2026/01/25
 */
@Slf4j
@Repository
public class AttendeeRepositoryImpl implements AttendeeRepository {
    @Autowired
    private AttendeeMapper attendeeMapper;

    @Override
    public AttendeeEntity save(AttendeeEntity attendee) {
        // 先尝试更新，如果影响行数为0则插入
        int updated = attendeeMapper.update(attendee);
        if (updated == 0) {
            attendeeMapper.insert(attendee);
        }
        return attendee;
    }

    @Override
    public List<AttendeeEntity> saveAll(List<AttendeeEntity> attendees) {
        if (attendees.isEmpty()) {
            return attendees;
        }
        attendeeMapper.insertBatch(attendees);
        return attendees;
    }

    @Override
    public Optional<AttendeeEntity> findById(String attendeeId) {
        return attendeeMapper.findById(attendeeId);
    }

    @Override
    public List<AttendeeEntity> findByEventId(String eventId) {
        return attendeeMapper.findByEventId(eventId);
    }

    @Override
    public List<AttendeeEntity> findByCustomerId(String customerId) {
        return attendeeMapper.findByCustomerId(customerId);
    }

    @Override
    public Optional<AttendeeEntity> findByEventIdAndCustomerId(String eventId, String customerId) {
        return attendeeMapper.findByEventIdAndCustomerId(eventId, customerId);
    }

    @Override
    public List<AttendeeEntity> findByEventIdAndRsvpStatus(String eventId, RsvpStatus status) {
        return attendeeMapper.findByEventIdAndRsvpStatus(eventId, status.getCode());
    }

    @Override
    public List<AttendeeEntity> findByEventIdAndRole(String eventId, AttendeeRole role) {
        return attendeeMapper.findByEventIdAndRole(eventId, role.getCode());
    }

    @Override
    public boolean existsById(String attendeeId) {
        return attendeeMapper.existsById(attendeeId);
    }

    @Override
    public boolean existsByEventIdAndCustomerId(String eventId, String customerId) {
        return attendeeMapper.existsByEventIdAndCustomerId(eventId, customerId);
    }

    @Override
    public long countByEventId(String eventId) {
        return attendeeMapper.countByEventId(eventId);
    }

    @Override
    public long countAcceptedByEventId(String eventId) {
        return attendeeMapper.countAcceptedByEventId(eventId);
    }

    @Override
    public void deleteById(String attendeeId) {
        attendeeMapper.deleteById(attendeeId);
    }

    @Override
    public void deleteAllById(List<String> attendeeIds) {
        attendeeMapper.deleteBatch(attendeeIds);
    }

    @Override
    public void deleteAllByEventId(String eventId) {
        attendeeMapper.deleteAllByEventId(eventId);
    }
}
