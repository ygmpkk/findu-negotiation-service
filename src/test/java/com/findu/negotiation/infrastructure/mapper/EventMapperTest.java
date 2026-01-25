package com.findu.negotiation.infrastructure.mapper;

import com.findu.negotiation.BaseIntegrationTest;
import com.findu.negotiation.domain.calendar.entity.EventEntity;
import com.findu.negotiation.domain.calendar.enums.EventType;
import com.findu.negotiation.domain.calendar.enums.EventStatus;
import com.findu.negotiation.domain.calendar.enums.Visibility;
import com.findu.negotiation.domain.calendar.enums.FreeBusyStatus;
import com.findu.negotiation.domain.calendar.vo.TimeInfoVO;
import com.findu.negotiation.domain.calendar.vo.LocationVO;
import com.findu.negotiation.domain.calendar.vo.ReminderVO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * EventMapper 单元测试
 *
 * @author timothy
 * @date 2026/01/25
 */
@DisplayName("EventMapper 单元测试")
@Sql(scripts = "/schema/calendar_schema.sql")
class EventMapperTest extends BaseIntegrationTest {

    @Autowired
    private EventMapper eventMapper;

    @Test
    @DisplayName("插入日程 - 成功")
    void testInsert_Success() {
        // Given
        String eventId = testDataManager.generateEventId();
        String calendarId = testDataManager.generateCalendarId();

        TimeInfoVO startTime = TimeInfoVO.forSpecificTime("2026-01-25T10:00:00", "Asia/Shanghai");
        TimeInfoVO endTime = TimeInfoVO.forSpecificTime("2026-01-25T11:00:00", "Asia/Shanghai");

        EventEntity event = EventEntity.builder()
                .eventId(eventId)
                .calendarId(calendarId)
                .title("测试日程")
                .description("这是一个测试日程")
                .visibility(Visibility.PRIVATE.getCode())
                .startTime(startTime)
                .endTime(endTime)
                .eventType(EventType.SINGLE.getCode())
                .status(EventStatus.SCHEDULED.getCode())
                .freeBusyStatus(FreeBusyStatus.BUSY.getCode())
                .isException(0)
                .reminderVOS(List.of(ReminderVO.builder().minutes(15).build()))
                .locationVO(LocationVO.builder().name("会议室").build())
                .build();

        // When
        int rows = eventMapper.insert(event);

        // Then
        assertEquals(1, rows);
        assertNotNull(event.getId());
        assertEquals(eventId, event.getEventId());

        // Verify via findById
        Optional<EventEntity> found = eventMapper.findById(eventId);
        assertTrue(found.isPresent());
        assertEquals(calendarId, found.get().getCalendarId());
        assertEquals("测试日程", found.get().getTitle());
        assertNotNull(found.get().getGmtCreate());
        assertNotNull(found.get().getGmtModify());
    }

    @Test
    @DisplayName("插入日程 - 重复日程")
    void testInsert_RecurringEvent() {
        // Given
        String eventId = testDataManager.generateEventId();
        String calendarId = testDataManager.generateCalendarId();

        TimeInfoVO startTime = TimeInfoVO.forSpecificTime("2026-01-25T10:00:00", "Asia/Shanghai");
        TimeInfoVO endTime = TimeInfoVO.forSpecificTime("2026-01-25T11:00:00", "Asia/Shanghai");

        EventEntity event = EventEntity.builder()
                .eventId(eventId)
                .calendarId(calendarId)
                .title("每周例会")
                .description("每周一上午10点的例会")
                .visibility(Visibility.PRIVATE.getCode())
                .startTime(startTime)
                .endTime(endTime)
                .eventType(EventType.RECURRING.getCode())
                .recurrence("FREQ=WEEKLY;BYDAY=MO")
                .status(EventStatus.SCHEDULED.getCode())
                .freeBusyStatus(FreeBusyStatus.BUSY.getCode())
                .isException(0)
                .reminderVOS(List.of())
                .locationVO(null)
                .build();

        // When
        int rows = eventMapper.insert(event);

        // Then
        assertEquals(1, rows);
        assertEquals(EventType.RECURRING.getCode(), event.getEventType());
        assertEquals("FREQ=WEEKLY;BYDAY=MO", event.getRecurrence());
    }

    @Test
    @DisplayName("更新日程 - 成功")
    void testUpdate_Success() {
        // Given - 先插入一个日程
        String eventId = testDataManager.generateEventId();
        String calendarId = testDataManager.generateCalendarId();

        TimeInfoVO startTime = TimeInfoVO.forSpecificTime("2026-01-25T10:00:00", "Asia/Shanghai");
        TimeInfoVO endTime = TimeInfoVO.forSpecificTime("2026-01-25T11:00:00", "Asia/Shanghai");

        EventEntity event = EventEntity.builder()
                .eventId(eventId)
                .calendarId(calendarId)
                .title("原始标题")
                .description("原始描述")
                .visibility(Visibility.PRIVATE.getCode())
                .startTime(startTime)
                .endTime(endTime)
                .eventType(EventType.SINGLE.getCode())
                .status(EventStatus.SCHEDULED.getCode())
                .freeBusyStatus(FreeBusyStatus.BUSY.getCode())
                .isException(0)
                .reminderVOS(List.of())
                .locationVO(null)
                .build();

        eventMapper.insert(event);

        // When - 更新日程
        EventEntity updated = EventEntity.builder()
                .eventId(eventId)
                .calendarId(calendarId)
                .title("更新后的标题")
                .description("更新后的描述")
                .visibility(Visibility.PUBLIC.getCode())
                .startTime(startTime)
                .endTime(endTime)
                .eventType(EventType.RECURRING.getCode())
                .status(EventStatus.CANCELLED.getCode())
                .freeBusyStatus(FreeBusyStatus.FREE.getCode())
                .isException(1)
                .recurrence("FREQ=DAILY")
                .reminderVOS(List.of())
                .locationVO(null)
                .build();

        int rows = eventMapper.update(updated);

        // Then
        assertEquals(1, rows);

        // Verify via findById
        Optional<EventEntity> found = eventMapper.findById(eventId);
        assertTrue(found.isPresent());
        assertEquals("更新后的标题", found.get().getTitle());
        assertEquals("更新后的描述", found.get().getDescription());
        assertEquals(Visibility.PUBLIC.getCode(), found.get().getVisibility());
        assertEquals(EventType.RECURRING.getCode(), found.get().getEventType());
        assertEquals(EventStatus.CANCELLED.getCode(), found.get().getStatus());
    }

    @Test
    @DisplayName("批量插入日程 - 成功")
    void testInsertBatch_Success() {
        // Given
        String calendarId = testDataManager.generateCalendarId();

        TimeInfoVO startTime = TimeInfoVO.forSpecificTime("2026-01-25T10:00:00", "Asia/Shanghai");
        TimeInfoVO endTime = TimeInfoVO.forSpecificTime("2026-01-25T11:00:00", "Asia/Shanghai");

        EventEntity event1 = EventEntity.builder()
                .eventId(testDataManager.generateEventId())
                .calendarId(calendarId)
                .title("日程1")
                .startTime(startTime)
                .endTime(endTime)
                .eventType(EventType.SINGLE.getCode())
                .status(EventStatus.SCHEDULED.getCode())
                .freeBusyStatus(FreeBusyStatus.BUSY.getCode())
                .isException(0)
                .reminderVOS(List.of())
                .locationVO(null)
                .build();

        EventEntity event2 = EventEntity.builder()
                .eventId(testDataManager.generateEventId())
                .calendarId(calendarId)
                .title("日程2")
                .startTime(startTime)
                .endTime(endTime)
                .eventType(EventType.SINGLE.getCode())
                .status(EventStatus.SCHEDULED.getCode())
                .freeBusyStatus(FreeBusyStatus.BUSY.getCode())
                .isException(0)
                .reminderVOS(List.of())
                .locationVO(null)
                .build();

        // When
        int rows = eventMapper.insertBatch(List.of(event1, event2));

        // Then
        assertEquals(2, rows);
        assertNotNull(event1.getId());
        assertNotNull(event2.getId());
    }

    @Test
    @DisplayName("根据ID查找日程 - 存在")
    void testFindById_Exists() {
        // Given
        String eventId = testDataManager.generateEventId();
        String calendarId = testDataManager.generateCalendarId();

        TimeInfoVO startTime = TimeInfoVO.forSpecificTime("2026-01-25T10:00:00", "Asia/Shanghai");
        TimeInfoVO endTime = TimeInfoVO.forSpecificTime("2026-01-25T11:00:00", "Asia/Shanghai");

        EventEntity event = EventEntity.builder()
                .eventId(eventId)
                .calendarId(calendarId)
                .title("测试日程")
                .startTime(startTime)
                .endTime(endTime)
                .eventType(EventType.SINGLE.getCode())
                .status(EventStatus.SCHEDULED.getCode())
                .freeBusyStatus(FreeBusyStatus.BUSY.getCode())
                .isException(0)
                .reminderVOS(List.of())
                .locationVO(null)
                .build();

        eventMapper.insert(event);

        // When
        Optional<EventEntity> found = eventMapper.findById(eventId);

        // Then
        assertTrue(found.isPresent());
        assertEquals(eventId, found.get().getEventId());
        assertEquals("测试日程", found.get().getTitle());
    }

    @Test
    @DisplayName("根据ID查找日程 - 不存在")
    void testFindById_NotExists() {
        // When
        Optional<EventEntity> found = eventMapper.findById("non_existent_id");

        // Then
        assertFalse(found.isPresent());
    }

    @Test
    @DisplayName("根据日历ID查找日程列表")
    void testFindByCalendarId() {
        // Given
        String calendarId = testDataManager.generateCalendarId();
        String otherCalendarId = testDataManager.generateCalendarId();

        TimeInfoVO startTime = TimeInfoVO.forSpecificTime("2026-01-25T10:00:00", "Asia/Shanghai");
        TimeInfoVO endTime = TimeInfoVO.forSpecificTime("2026-01-25T11:00:00", "Asia/Shanghai");

        EventEntity event1 = EventEntity.builder()
                .eventId(testDataManager.generateEventId())
                .calendarId(calendarId)
                .title("日程1")
                .startTime(startTime)
                .endTime(endTime)
                .eventType(EventType.SINGLE.getCode())
                .status(EventStatus.SCHEDULED.getCode())
                .freeBusyStatus(FreeBusyStatus.BUSY.getCode())
                .isException(0)
                .reminderVOS(List.of())
                .locationVO(null)
                .build();

        EventEntity event2 = EventEntity.builder()
                .eventId(testDataManager.generateEventId())
                .calendarId(calendarId)
                .title("日程2")
                .startTime(startTime)
                .endTime(endTime)
                .eventType(EventType.SINGLE.getCode())
                .status(EventStatus.SCHEDULED.getCode())
                .freeBusyStatus(FreeBusyStatus.BUSY.getCode())
                .isException(0)
                .reminderVOS(List.of())
                .locationVO(null)
                .build();

        EventEntity otherEvent = EventEntity.builder()
                .eventId(testDataManager.generateEventId())
                .calendarId(otherCalendarId)
                .title("其他日程")
                .startTime(startTime)
                .endTime(endTime)
                .eventType(EventType.SINGLE.getCode())
                .status(EventStatus.SCHEDULED.getCode())
                .freeBusyStatus(FreeBusyStatus.BUSY.getCode())
                .isException(0)
                .reminderVOS(List.of())
                .locationVO(null)
                .build();

        eventMapper.insert(event1);
        eventMapper.insert(event2);
        eventMapper.insert(otherEvent);

        // When
        List<EventEntity> events = eventMapper.findByCalendarId(calendarId);

        // Then
        assertEquals(2, events.size());
        assertTrue(events.stream().anyMatch(e -> e.getTitle().equals("日程1")));
        assertTrue(events.stream().anyMatch(e -> e.getTitle().equals("日程2")));
        assertFalse(events.stream().anyMatch(e -> e.getTitle().equals("其他日程")));
    }

    @Test
    @DisplayName("根据日历ID和状态查找日程列表")
    void testFindByCalendarIdAndStatus() {
        // Given
        String calendarId = testDataManager.generateCalendarId();

        TimeInfoVO startTime = TimeInfoVO.forSpecificTime("2026-01-25T10:00:00", "Asia/Shanghai");
        TimeInfoVO endTime = TimeInfoVO.forSpecificTime("2026-01-25T11:00:00", "Asia/Shanghai");

        EventEntity scheduledEvent = EventEntity.builder()
                .eventId(testDataManager.generateEventId())
                .calendarId(calendarId)
                .title("已安排日程")
                .startTime(startTime)
                .endTime(endTime)
                .eventType(EventType.SINGLE.getCode())
                .status(EventStatus.SCHEDULED.getCode())
                .freeBusyStatus(FreeBusyStatus.BUSY.getCode())
                .isException(0)
                .reminderVOS(List.of())
                .locationVO(null)
                .build();

        EventEntity cancelledEvent = EventEntity.builder()
                .eventId(testDataManager.generateEventId())
                .calendarId(calendarId)
                .title("已取消日程")
                .startTime(startTime)
                .endTime(endTime)
                .eventType(EventType.SINGLE.getCode())
                .status(EventStatus.CANCELLED.getCode())
                .freeBusyStatus(FreeBusyStatus.BUSY.getCode())
                .isException(0)
                .reminderVOS(List.of())
                .locationVO(null)
                .build();

        eventMapper.insert(scheduledEvent);
        eventMapper.insert(cancelledEvent);

        // When - 使用Integer参数
        List<EventEntity> scheduledEvents = eventMapper.findByCalendarIdAndStatus(calendarId, EventStatus.SCHEDULED.getCode());
        List<EventEntity> cancelledEvents = eventMapper.findByCalendarIdAndStatus(calendarId, EventStatus.CANCELLED.getCode());

        // Then
        assertEquals(1, scheduledEvents.size());
        assertEquals("已安排日程", scheduledEvents.get(0).getTitle());
        assertEquals(1, cancelledEvents.size());
        assertEquals("已取消日程", cancelledEvents.get(0).getTitle());
    }

    @Test
    @DisplayName("根据日历ID和日程类型查找日程列表")
    void testFindByCalendarIdAndEventType() {
        // Given
        String calendarId = testDataManager.generateCalendarId();

        TimeInfoVO startTime = TimeInfoVO.forSpecificTime("2026-01-25T10:00:00", "Asia/Shanghai");
        TimeInfoVO endTime = TimeInfoVO.forSpecificTime("2026-01-25T11:00:00", "Asia/Shanghai");

        EventEntity singleEvent = EventEntity.builder()
                .eventId(testDataManager.generateEventId())
                .calendarId(calendarId)
                .title("普通日程")
                .startTime(startTime)
                .endTime(endTime)
                .eventType(EventType.SINGLE.getCode())
                .status(EventStatus.SCHEDULED.getCode())
                .freeBusyStatus(FreeBusyStatus.BUSY.getCode())
                .isException(0)
                .reminderVOS(List.of())
                .locationVO(null)
                .build();

        EventEntity recurringEvent = EventEntity.builder()
                .eventId(testDataManager.generateEventId())
                .calendarId(calendarId)
                .title("重复日程")
                .recurrence("FREQ=DAILY")
                .startTime(startTime)
                .endTime(endTime)
                .eventType(EventType.RECURRING.getCode())
                .status(EventStatus.SCHEDULED.getCode())
                .freeBusyStatus(FreeBusyStatus.BUSY.getCode())
                .isException(0)
                .reminderVOS(List.of())
                .locationVO(null)
                .build();

        eventMapper.insert(singleEvent);
        eventMapper.insert(recurringEvent);

        // When - 使用Integer参数
        List<EventEntity> singleEvents = eventMapper.findByCalendarIdAndEventType(calendarId, EventType.SINGLE.getCode());
        List<EventEntity> recurringEvents = eventMapper.findByCalendarIdAndEventType(calendarId, EventType.RECURRING.getCode());

        // Then
        assertEquals(1, singleEvents.size());
        assertEquals("普通日程", singleEvents.get(0).getTitle());
        assertEquals(1, recurringEvents.size());
        assertEquals("重复日程", recurringEvents.get(0).getTitle());
    }

    @Test
    @DisplayName("删除日程")
    void testDeleteById() {
        // Given
        String eventId = testDataManager.generateEventId();
        String calendarId = testDataManager.generateCalendarId();

        TimeInfoVO startTime = TimeInfoVO.forSpecificTime("2026-01-25T10:00:00", "Asia/Shanghai");
        TimeInfoVO endTime = TimeInfoVO.forSpecificTime("2026-01-25T11:00:00", "Asia/Shanghai");

        EventEntity event = EventEntity.builder()
                .eventId(eventId)
                .calendarId(calendarId)
                .title("待删除日程")
                .startTime(startTime)
                .endTime(endTime)
                .eventType(EventType.SINGLE.getCode())
                .status(EventStatus.SCHEDULED.getCode())
                .freeBusyStatus(FreeBusyStatus.BUSY.getCode())
                .isException(0)
                .reminderVOS(List.of())
                .locationVO(null)
                .build();

        eventMapper.insert(event);

        // When
        int rows = eventMapper.deleteById(eventId);

        // Then
        assertEquals(1, rows);
        Optional<EventEntity> found = eventMapper.findById(eventId);
        assertFalse(found.isPresent());
    }

    @Test
    @DisplayName("检查日程是否存在")
    void testExistsById() {
        // Given
        String eventId = testDataManager.generateEventId();
        String calendarId = testDataManager.generateCalendarId();

        TimeInfoVO startTime = TimeInfoVO.forSpecificTime("2026-01-25T10:00:00", "Asia/Shanghai");
        TimeInfoVO endTime = TimeInfoVO.forSpecificTime("2026-01-25T11:00:00", "Asia/Shanghai");

        EventEntity event = EventEntity.builder()
                .eventId(eventId)
                .calendarId(calendarId)
                .title("测试日程")
                .startTime(startTime)
                .endTime(endTime)
                .eventType(EventType.SINGLE.getCode())
                .status(EventStatus.SCHEDULED.getCode())
                .freeBusyStatus(FreeBusyStatus.BUSY.getCode())
                .isException(0)
                .reminderVOS(List.of())
                .locationVO(null)
                .build();

        eventMapper.insert(event);

        // When & Then
        assertTrue(eventMapper.existsById(eventId));
        assertFalse(eventMapper.existsById("non_existent_id"));
    }

    @Test
    @DisplayName("查找重复日程")
    void testFindRecurringEvents() {
        // Given
        String calendarId = testDataManager.generateCalendarId();

        TimeInfoVO startTime = TimeInfoVO.forSpecificTime("2026-01-25T10:00:00", "Asia/Shanghai");
        TimeInfoVO endTime = TimeInfoVO.forSpecificTime("2026-01-25T11:00:00", "Asia/Shanghai");

        EventEntity recurringEvent = EventEntity.builder()
                .eventId(testDataManager.generateEventId())
                .calendarId(calendarId)
                .title("重复日程")
                .recurrence("FREQ=DAILY")
                .isException(0)
                .startTime(startTime)
                .endTime(endTime)
                .eventType(EventType.RECURRING.getCode())
                .status(EventStatus.SCHEDULED.getCode())
                .freeBusyStatus(FreeBusyStatus.BUSY.getCode())
                .reminderVOS(List.of())
                .locationVO(null)
                .build();

        EventEntity singleEvent = EventEntity.builder()
                .eventId(testDataManager.generateEventId())
                .calendarId(calendarId)
                .title("普通日程")
                .startTime(startTime)
                .endTime(endTime)
                .eventType(EventType.SINGLE.getCode())
                .status(EventStatus.SCHEDULED.getCode())
                .freeBusyStatus(FreeBusyStatus.BUSY.getCode())
                .isException(0)
                .reminderVOS(List.of())
                .locationVO(null)
                .build();

        eventMapper.insertBatch(List.of(recurringEvent, singleEvent));

        // When
        List<EventEntity> recurringEvents = eventMapper.findRecurringEvents(calendarId);

        // Then
        assertEquals(1, recurringEvents.size());
        assertEquals("重复日程", recurringEvents.get(0).getTitle());
        assertTrue(recurringEvents.get(0).isRecurring());
        assertFalse(recurringEvents.get(0).isException());
    }

    @Test
    @DisplayName("删除日历下的所有日程")
    void testDeleteAllByCalendarId() {
        // Given
        String calendarId = testDataManager.generateCalendarId();

        TimeInfoVO startTime = TimeInfoVO.forSpecificTime("2026-01-25T10:00:00", "Asia/Shanghai");
        TimeInfoVO endTime = TimeInfoVO.forSpecificTime("2026-01-25T11:00:00", "Asia/Shanghai");

        EventEntity event1 = EventEntity.builder()
                .eventId(testDataManager.generateEventId())
                .calendarId(calendarId)
                .title("日程1")
                .startTime(startTime)
                .endTime(endTime)
                .eventType(EventType.SINGLE.getCode())
                .status(EventStatus.SCHEDULED.getCode())
                .freeBusyStatus(FreeBusyStatus.BUSY.getCode())
                .isException(0)
                .reminderVOS(List.of())
                .locationVO(null)
                .build();

        EventEntity event2 = EventEntity.builder()
                .eventId(testDataManager.generateEventId())
                .calendarId(calendarId)
                .title("日程2")
                .startTime(startTime)
                .endTime(endTime)
                .eventType(EventType.SINGLE.getCode())
                .status(EventStatus.SCHEDULED.getCode())
                .freeBusyStatus(FreeBusyStatus.BUSY.getCode())
                .isException(0)
                .reminderVOS(List.of())
                .locationVO(null)
                .build();

        eventMapper.insertBatch(List.of(event1, event2));

        // When
        int rows = eventMapper.deleteAllByCalendarId(calendarId);

        // Then
        assertTrue(rows > 0);
        List<EventEntity> remaining = eventMapper.findByCalendarId(calendarId);
        assertTrue(remaining.isEmpty());
    }

    @Test
    @DisplayName("批量删除日程")
    void testDeleteBatch() {
        // Given
        String calendarId = testDataManager.generateCalendarId();

        TimeInfoVO startTime = TimeInfoVO.forSpecificTime("2026-01-25T10:00:00", "Asia/Shanghai");
        TimeInfoVO endTime = TimeInfoVO.forSpecificTime("2026-01-25T11:00:00", "Asia/Shanghai");

        String eventId1 = testDataManager.generateEventId();
        String eventId2 = testDataManager.generateEventId();

        EventEntity event1 = EventEntity.builder()
                .eventId(eventId1)
                .calendarId(calendarId)
                .title("日程1")
                .startTime(startTime)
                .endTime(endTime)
                .eventType(EventType.SINGLE.getCode())
                .status(EventStatus.SCHEDULED.getCode())
                .freeBusyStatus(FreeBusyStatus.BUSY.getCode())
                .isException(0)
                .reminderVOS(List.of())
                .locationVO(null)
                .build();

        EventEntity event2 = EventEntity.builder()
                .eventId(eventId2)
                .calendarId(calendarId)
                .title("日程2")
                .startTime(startTime)
                .endTime(endTime)
                .eventType(EventType.SINGLE.getCode())
                .status(EventStatus.SCHEDULED.getCode())
                .freeBusyStatus(FreeBusyStatus.BUSY.getCode())
                .isException(0)
                .reminderVOS(List.of())
                .locationVO(null)
                .build();

        eventMapper.insertBatch(List.of(event1, event2));

        // When
        int rows = eventMapper.deleteBatch(List.of(eventId1, eventId2));

        // Then
        assertEquals(2, rows);
        assertFalse(eventMapper.existsById(eventId1));
        assertFalse(eventMapper.existsById(eventId2));
    }
}
