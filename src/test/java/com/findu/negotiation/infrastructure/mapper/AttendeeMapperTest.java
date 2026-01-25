package com.findu.negotiation.infrastructure.mapper;

import com.findu.negotiation.BaseIntegrationTest;
import com.findu.negotiation.domain.calendar.entity.AttendeeEntity;
import com.findu.negotiation.domain.calendar.enums.AttendeeRole;
import com.findu.negotiation.domain.calendar.enums.RsvpStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * AttendeeMapper 单元测试
 *
 * @author timothy
 * @date 2026/01/25
 */
@DisplayName("AttendeeMapper 单元测试")
@Sql(scripts = "/schema/calendar_schema.sql")
class AttendeeMapperTest extends BaseIntegrationTest {

    @Autowired
    private AttendeeMapper attendeeMapper;

    @Test
    @DisplayName("插入参与人 - 成功")
    void testInsert_Success() {
        // Given
        String attendeeId = testDataManager.generateAttendeeId();
        String eventId = testDataManager.generateEventId();
        String customerId = testDataManager.generateCustomerId();

        AttendeeEntity attendee = AttendeeEntity.builder()
                .attendeeId(attendeeId)
                .eventId(eventId)
                .customerId(customerId)
                .role(AttendeeRole.REQUIRED.getCode())
                .rsvpStatus(RsvpStatus.PENDING.getCode())
                .build();

        // When
        int rows = attendeeMapper.insert(attendee);

        // Then
        assertEquals(1, rows);
        assertNotNull(attendee.getId());
        assertEquals(attendeeId, attendee.getAttendeeId());

        // Verify via findById
        Optional<AttendeeEntity> found = attendeeMapper.findById(attendeeId);
        assertTrue(found.isPresent());
        assertEquals(eventId, found.get().getEventId());
        assertEquals(customerId, found.get().getCustomerId());
        assertEquals(AttendeeRole.REQUIRED.getCode(), found.get().getRole());
        assertEquals(RsvpStatus.PENDING.getCode(), found.get().getRsvpStatus());
        assertNotNull(found.get().getGmtCreate());
    }

    @Test
    @DisplayName("更新参与人 - 成功")
    void testUpdate_Success() {
        // Given - 先插入一个参与人
        String attendeeId = testDataManager.generateAttendeeId();
        String eventId = testDataManager.generateEventId();
        String customerId = testDataManager.generateCustomerId();

        AttendeeEntity attendee = AttendeeEntity.builder()
                .attendeeId(attendeeId)
                .eventId(eventId)
                .customerId(customerId)
                .role(AttendeeRole.REQUIRED.getCode())
                .rsvpStatus(RsvpStatus.PENDING.getCode())
                .build();

        attendeeMapper.insert(attendee);

        // When - 更新参与人
        AttendeeEntity updated = AttendeeEntity.builder()
                .attendeeId(attendeeId)
                .eventId(eventId)
                .customerId(customerId)
                .role(AttendeeRole.OPTIONAL.getCode())
                .rsvpStatus(RsvpStatus.ACCEPTED.getCode())
                .build();

        int rows = attendeeMapper.update(updated);

        // Then
        assertEquals(1, rows);

        // Verify via findById
        Optional<AttendeeEntity> found = attendeeMapper.findById(attendeeId);
        assertTrue(found.isPresent());
        assertEquals(AttendeeRole.OPTIONAL.getCode(), found.get().getRole());
        assertEquals(RsvpStatus.ACCEPTED.getCode(), found.get().getRsvpStatus());
    }

    @Test
    @DisplayName("批量插入参与人 - 成功")
    void testInsertBatch_Success() {
        // Given
        String eventId = testDataManager.generateEventId();

        AttendeeEntity attendee1 = AttendeeEntity.builder()
                .attendeeId(testDataManager.generateAttendeeId())
                .eventId(eventId)
                .customerId(testDataManager.generateCustomerId())
                .role(AttendeeRole.REQUIRED.getCode())
                .rsvpStatus(RsvpStatus.ACCEPTED.getCode())
                .build();

        AttendeeEntity attendee2 = AttendeeEntity.builder()
                .attendeeId(testDataManager.generateAttendeeId())
                .eventId(eventId)
                .customerId(testDataManager.generateCustomerId())
                .role(AttendeeRole.OPTIONAL.getCode())
                .rsvpStatus(RsvpStatus.PENDING.getCode())
                .build();

        // When
        int rows = attendeeMapper.insertBatch(List.of(attendee1, attendee2));

        // Then
        assertEquals(2, rows);
        assertNotNull(attendee1.getId());
        assertNotNull(attendee2.getId());
    }

    @Test
    @DisplayName("根据ID查找参与人 - 存在")
    void testFindById_Exists() {
        // Given
        String attendeeId = testDataManager.generateAttendeeId();
        String eventId = testDataManager.generateEventId();
        String customerId = testDataManager.generateCustomerId();

        AttendeeEntity attendee = AttendeeEntity.builder()
                .attendeeId(attendeeId)
                .eventId(eventId)
                .customerId(customerId)
                .role(AttendeeRole.REQUIRED.getCode())
                .rsvpStatus(RsvpStatus.PENDING.getCode())
                .build();

        attendeeMapper.insert(attendee);

        // When
        Optional<AttendeeEntity> found = attendeeMapper.findById(attendeeId);

        // Then
        assertTrue(found.isPresent());
        assertEquals(attendeeId, found.get().getAttendeeId());
        assertEquals(eventId, found.get().getEventId());
        assertEquals(customerId, found.get().getCustomerId());
    }

    @Test
    @DisplayName("根据ID查找参与人 - 不存在")
    void testFindById_NotExists() {
        // When
        Optional<AttendeeEntity> found = attendeeMapper.findById("non_existent_id");

        // Then
        assertFalse(found.isPresent());
    }

    @Test
    @DisplayName("根据日程ID查找参与人列表")
    void testFindByEventId() {
        // Given
        String eventId = testDataManager.generateEventId();
        String otherEventId = testDataManager.generateEventId();

        AttendeeEntity attendee1 = AttendeeEntity.builder()
                .attendeeId(testDataManager.generateAttendeeId())
                .eventId(eventId)
                .customerId(testDataManager.generateCustomerId())
                .role(AttendeeRole.REQUIRED.getCode())
                .rsvpStatus(RsvpStatus.ACCEPTED.getCode())
                .build();

        AttendeeEntity attendee2 = AttendeeEntity.builder()
                .attendeeId(testDataManager.generateAttendeeId())
                .eventId(eventId)
                .customerId(testDataManager.generateCustomerId())
                .role(AttendeeRole.OPTIONAL.getCode())
                .rsvpStatus(RsvpStatus.PENDING.getCode())
                .build();

        AttendeeEntity otherAttendee = AttendeeEntity.builder()
                .attendeeId(testDataManager.generateAttendeeId())
                .eventId(otherEventId)
                .customerId(testDataManager.generateCustomerId())
                .role(AttendeeRole.REQUIRED.getCode())
                .rsvpStatus(RsvpStatus.PENDING.getCode())
                .build();

        attendeeMapper.insert(attendee1);
        attendeeMapper.insert(attendee2);
        attendeeMapper.insert(otherAttendee);

        // When
        List<AttendeeEntity> attendees = attendeeMapper.findByEventId(eventId);

        // Then
        assertEquals(2, attendees.size());
        assertTrue(attendees.stream().allMatch(a -> eventId.equals(a.getEventId())));
    }

    @Test
    @DisplayName("根据客户ID查找参与人列表")
    void testFindByCustomerId() {
        // Given
        String customerId = testDataManager.generateCustomerId();
        String otherCustomerId = testDataManager.generateCustomerId();

        AttendeeEntity attendee1 = AttendeeEntity.builder()
                .attendeeId(testDataManager.generateAttendeeId())
                .eventId(testDataManager.generateEventId())
                .customerId(customerId)
                .role(AttendeeRole.REQUIRED.getCode())
                .rsvpStatus(RsvpStatus.ACCEPTED.getCode())
                .build();

        AttendeeEntity attendee2 = AttendeeEntity.builder()
                .attendeeId(testDataManager.generateAttendeeId())
                .eventId(testDataManager.generateEventId())
                .customerId(customerId)
                .role(AttendeeRole.REQUIRED.getCode())
                .rsvpStatus(RsvpStatus.PENDING.getCode())
                .build();

        AttendeeEntity otherAttendee = AttendeeEntity.builder()
                .attendeeId(testDataManager.generateAttendeeId())
                .eventId(testDataManager.generateEventId())
                .customerId(otherCustomerId)
                .role(AttendeeRole.REQUIRED.getCode())
                .rsvpStatus(RsvpStatus.PENDING.getCode())
                .build();

        attendeeMapper.insertBatch(List.of(attendee1, attendee2, otherAttendee));

        // When
        List<AttendeeEntity> attendees = attendeeMapper.findByCustomerId(customerId);

        // Then
        assertEquals(2, attendees.size());
        assertTrue(attendees.stream().allMatch(a -> customerId.equals(a.getCustomerId())));
    }

    @Test
    @DisplayName("根据日程ID和客户ID查找参与人")
    void testFindByEventIdAndCustomerId() {
        // Given
        String eventId = testDataManager.generateEventId();
        String customerId = testDataManager.generateCustomerId();

        AttendeeEntity attendee = AttendeeEntity.builder()
                .attendeeId(testDataManager.generateAttendeeId())
                .eventId(eventId)
                .customerId(customerId)
                .role(AttendeeRole.REQUIRED.getCode())
                .rsvpStatus(RsvpStatus.ACCEPTED.getCode())
                .build();

        attendeeMapper.insert(attendee);

        // When
        Optional<AttendeeEntity> found = attendeeMapper.findByEventIdAndCustomerId(eventId, customerId);

        // Then
        assertTrue(found.isPresent());
        assertEquals(eventId, found.get().getEventId());
        assertEquals(customerId, found.get().getCustomerId());

        // 测试不存在的情况
        Optional<AttendeeEntity> notFound = attendeeMapper.findByEventIdAndCustomerId(eventId, "other_customer");
        assertFalse(notFound.isPresent());
    }

    @Test
    @DisplayName("根据日程ID和RSVP状态查找参与人列表")
    void testFindByEventIdAndRsvpStatus() {
        // Given
        String eventId = testDataManager.generateEventId();

        AttendeeEntity accepted1 = AttendeeEntity.builder()
                .attendeeId(testDataManager.generateAttendeeId())
                .eventId(eventId)
                .customerId(testDataManager.generateCustomerId())
                .role(AttendeeRole.REQUIRED.getCode())
                .rsvpStatus(RsvpStatus.ACCEPTED.getCode())
                .build();

        AttendeeEntity accepted2 = AttendeeEntity.builder()
                .attendeeId(testDataManager.generateAttendeeId())
                .eventId(eventId)
                .customerId(testDataManager.generateCustomerId())
                .role(AttendeeRole.REQUIRED.getCode())
                .rsvpStatus(RsvpStatus.ACCEPTED.getCode())
                .build();

        AttendeeEntity pending = AttendeeEntity.builder()
                .attendeeId(testDataManager.generateAttendeeId())
                .eventId(eventId)
                .customerId(testDataManager.generateCustomerId())
                .role(AttendeeRole.REQUIRED.getCode())
                .rsvpStatus(RsvpStatus.PENDING.getCode())
                .build();

        attendeeMapper.insertBatch(List.of(accepted1, accepted2, pending));

        // When - 使用Integer参数
        List<AttendeeEntity> acceptedAttendees = attendeeMapper.findByEventIdAndRsvpStatus(eventId, RsvpStatus.ACCEPTED.getCode());
        List<AttendeeEntity> pendingAttendees = attendeeMapper.findByEventIdAndRsvpStatus(eventId, RsvpStatus.PENDING.getCode());

        // Then
        assertEquals(2, acceptedAttendees.size());
        assertTrue(acceptedAttendees.stream().allMatch(a -> RsvpStatus.ACCEPTED.getCode().equals(a.getRsvpStatus())));

        assertEquals(1, pendingAttendees.size());
        assertEquals(RsvpStatus.PENDING.getCode(), pendingAttendees.get(0).getRsvpStatus());
    }

    @Test
    @DisplayName("根据日程ID和角色查找参与人列表")
    void testFindByEventIdAndRole() {
        // Given
        String eventId = testDataManager.generateEventId();

        AttendeeEntity required1 = AttendeeEntity.builder()
                .attendeeId(testDataManager.generateAttendeeId())
                .eventId(eventId)
                .customerId(testDataManager.generateCustomerId())
                .role(AttendeeRole.REQUIRED.getCode())
                .rsvpStatus(RsvpStatus.ACCEPTED.getCode())
                .build();

        AttendeeEntity required2 = AttendeeEntity.builder()
                .attendeeId(testDataManager.generateAttendeeId())
                .eventId(eventId)
                .customerId(testDataManager.generateCustomerId())
                .role(AttendeeRole.REQUIRED.getCode())
                .rsvpStatus(RsvpStatus.PENDING.getCode())
                .build();

        AttendeeEntity optional = AttendeeEntity.builder()
                .attendeeId(testDataManager.generateAttendeeId())
                .eventId(eventId)
                .customerId(testDataManager.generateCustomerId())
                .role(AttendeeRole.OPTIONAL.getCode())
                .rsvpStatus(RsvpStatus.PENDING.getCode())
                .build();

        attendeeMapper.insertBatch(List.of(required1, required2, optional));

        // When - 使用Integer参数
        List<AttendeeEntity> requiredAttendees = attendeeMapper.findByEventIdAndRole(eventId, AttendeeRole.REQUIRED.getCode());
        List<AttendeeEntity> optionalAttendees = attendeeMapper.findByEventIdAndRole(eventId, AttendeeRole.OPTIONAL.getCode());

        // Then
        assertEquals(2, requiredAttendees.size());
        assertTrue(requiredAttendees.stream().allMatch(a -> AttendeeRole.REQUIRED.getCode().equals(a.getRole())));

        assertEquals(1, optionalAttendees.size());
        assertEquals(AttendeeRole.OPTIONAL.getCode(), optionalAttendees.get(0).getRole());
    }

    @Test
    @DisplayName("删除参与人")
    void testDeleteById() {
        // Given
        String attendeeId = testDataManager.generateAttendeeId();
        String eventId = testDataManager.generateEventId();
        String customerId = testDataManager.generateCustomerId();

        AttendeeEntity attendee = AttendeeEntity.builder()
                .attendeeId(attendeeId)
                .eventId(eventId)
                .customerId(customerId)
                .role(AttendeeRole.REQUIRED.getCode())
                .rsvpStatus(RsvpStatus.PENDING.getCode())
                .build();

        attendeeMapper.insert(attendee);

        // When
        int rows = attendeeMapper.deleteById(attendeeId);

        // Then
        assertEquals(1, rows);
        Optional<AttendeeEntity> found = attendeeMapper.findById(attendeeId);
        assertFalse(found.isPresent());
    }

    @Test
    @DisplayName("删除日程的所有参与人")
    void testDeleteAllByEventId() {
        // Given
        String eventId = testDataManager.generateEventId();

        AttendeeEntity attendee1 = AttendeeEntity.builder()
                .attendeeId(testDataManager.generateAttendeeId())
                .eventId(eventId)
                .customerId(testDataManager.generateCustomerId())
                .role(AttendeeRole.REQUIRED.getCode())
                .rsvpStatus(RsvpStatus.ACCEPTED.getCode())
                .build();

        AttendeeEntity attendee2 = AttendeeEntity.builder()
                .attendeeId(testDataManager.generateAttendeeId())
                .eventId(eventId)
                .customerId(testDataManager.generateCustomerId())
                .role(AttendeeRole.OPTIONAL.getCode())
                .rsvpStatus(RsvpStatus.PENDING.getCode())
                .build();

        attendeeMapper.insertBatch(List.of(attendee1, attendee2));

        // When
        int rows = attendeeMapper.deleteAllByEventId(eventId);

        // Then
        assertTrue(rows > 0);
        List<AttendeeEntity> remaining = attendeeMapper.findByEventId(eventId);
        assertTrue(remaining.isEmpty());
    }

    @Test
    @DisplayName("批量删除参与人")
    void testDeleteBatch() {
        // Given
        String eventId = testDataManager.generateEventId();

        String attendeeId1 = testDataManager.generateAttendeeId();
        String attendeeId2 = testDataManager.generateAttendeeId();

        AttendeeEntity attendee1 = AttendeeEntity.builder()
                .attendeeId(attendeeId1)
                .eventId(eventId)
                .customerId(testDataManager.generateCustomerId())
                .role(AttendeeRole.REQUIRED.getCode())
                .rsvpStatus(RsvpStatus.ACCEPTED.getCode())
                .build();

        AttendeeEntity attendee2 = AttendeeEntity.builder()
                .attendeeId(attendeeId2)
                .eventId(eventId)
                .customerId(testDataManager.generateCustomerId())
                .role(AttendeeRole.OPTIONAL.getCode())
                .rsvpStatus(RsvpStatus.PENDING.getCode())
                .build();

        attendeeMapper.insertBatch(List.of(attendee1, attendee2));

        // When
        int rows = attendeeMapper.deleteBatch(List.of(attendeeId1, attendeeId2));

        // Then
        assertEquals(2, rows);
        assertFalse(attendeeMapper.existsById(attendeeId1));
        assertFalse(attendeeMapper.existsById(attendeeId2));
    }

    @Test
    @DisplayName("检查参与人是否存在")
    void testExistsById() {
        // Given
        String attendeeId = testDataManager.generateAttendeeId();
        String eventId = testDataManager.generateEventId();
        String customerId = testDataManager.generateCustomerId();

        AttendeeEntity attendee = AttendeeEntity.builder()
                .attendeeId(attendeeId)
                .eventId(eventId)
                .customerId(customerId)
                .role(AttendeeRole.REQUIRED.getCode())
                .rsvpStatus(RsvpStatus.PENDING.getCode())
                .build();

        attendeeMapper.insert(attendee);

        // When & Then
        assertTrue(attendeeMapper.existsById(attendeeId));
        assertFalse(attendeeMapper.existsById("non_existent_id"));
    }

    @Test
    @DisplayName("检查日程是否包含指定参与人")
    void testExistsByEventIdAndCustomerId() {
        // Given
        String eventId = testDataManager.generateEventId();
        String customerId = testDataManager.generateCustomerId();

        AttendeeEntity attendee = AttendeeEntity.builder()
                .attendeeId(testDataManager.generateAttendeeId())
                .eventId(eventId)
                .customerId(customerId)
                .role(AttendeeRole.REQUIRED.getCode())
                .rsvpStatus(RsvpStatus.PENDING.getCode())
                .build();

        attendeeMapper.insert(attendee);

        // When & Then
        assertTrue(attendeeMapper.existsByEventIdAndCustomerId(eventId, customerId));
        assertFalse(attendeeMapper.existsByEventIdAndCustomerId(eventId, "other_customer"));
    }

    @Test
    @DisplayName("统计日程的参与人数量")
    void testCountByEventId() {
        // Given
        String eventId = testDataManager.generateEventId();

        AttendeeEntity attendee1 = AttendeeEntity.builder()
                .attendeeId(testDataManager.generateAttendeeId())
                .eventId(eventId)
                .customerId(testDataManager.generateCustomerId())
                .role(AttendeeRole.REQUIRED.getCode())
                .rsvpStatus(RsvpStatus.ACCEPTED.getCode())
                .build();

        AttendeeEntity attendee2 = AttendeeEntity.builder()
                .attendeeId(testDataManager.generateAttendeeId())
                .eventId(eventId)
                .customerId(testDataManager.generateCustomerId())
                .role(AttendeeRole.OPTIONAL.getCode())
                .rsvpStatus(RsvpStatus.PENDING.getCode())
                .build();

        AttendeeEntity attendee3 = AttendeeEntity.builder()
                .attendeeId(testDataManager.generateAttendeeId())
                .eventId(eventId)
                .customerId(testDataManager.generateCustomerId())
                .role(AttendeeRole.REQUIRED.getCode())
                .rsvpStatus(RsvpStatus.DECLINED.getCode())
                .build();

        attendeeMapper.insertBatch(List.of(attendee1, attendee2, attendee3));

        // When
        long count = attendeeMapper.countByEventId(eventId);

        // Then
        assertEquals(3, count);
    }

    @Test
    @DisplayName("统计日程中已接受的参与人数量")
    void testCountAcceptedByEventId() {
        // Given
        String eventId = testDataManager.generateEventId();

        AttendeeEntity accepted1 = AttendeeEntity.builder()
                .attendeeId(testDataManager.generateAttendeeId())
                .eventId(eventId)
                .customerId(testDataManager.generateCustomerId())
                .role(AttendeeRole.REQUIRED.getCode())
                .rsvpStatus(RsvpStatus.ACCEPTED.getCode())
                .build();

        AttendeeEntity accepted2 = AttendeeEntity.builder()
                .attendeeId(testDataManager.generateAttendeeId())
                .eventId(eventId)
                .customerId(testDataManager.generateCustomerId())
                .role(AttendeeRole.REQUIRED.getCode())
                .rsvpStatus(RsvpStatus.ACCEPTED.getCode())
                .build();

        AttendeeEntity pending = AttendeeEntity.builder()
                .attendeeId(testDataManager.generateAttendeeId())
                .eventId(eventId)
                .customerId(testDataManager.generateCustomerId())
                .role(AttendeeRole.REQUIRED.getCode())
                .rsvpStatus(RsvpStatus.PENDING.getCode())
                .build();

        AttendeeEntity declined = AttendeeEntity.builder()
                .attendeeId(testDataManager.generateAttendeeId())
                .eventId(eventId)
                .customerId(testDataManager.generateCustomerId())
                .role(AttendeeRole.REQUIRED.getCode())
                .rsvpStatus(RsvpStatus.DECLINED.getCode())
                .build();

        attendeeMapper.insertBatch(List.of(accepted1, accepted2, pending, declined));

        // When
        long acceptedCount = attendeeMapper.countAcceptedByEventId(eventId);

        // Then
        assertEquals(2, acceptedCount);
    }
}
