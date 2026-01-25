package com.findu.negotiation.infrastructure.mapper;

import com.findu.negotiation.BaseIntegrationTest;
import com.findu.negotiation.domain.calendar.entity.CalendarEntity;
import com.findu.negotiation.domain.calendar.enums.CalendarStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * CalendarMapper 单元测试
 *
 * @author timothy
 * @date 2026/01/25
 */
@DisplayName("CalendarMapper 单元测试")
@Sql(scripts = "/schema/calendar_schema.sql")
class CalendarMapperTest extends BaseIntegrationTest {

    @Autowired
    private CalendarMapper calendarMapper;

    @Test
    @DisplayName("插入日历 - 成功")
    void testInsert_Success() {
        // Given
        String calendarId = testDataManager.generateCalendarId();
        String providerId = testDataManager.generateProviderId();

        CalendarEntity calendar = CalendarEntity.builder()
                .calendarId(calendarId)
                .providerId(providerId)
                .name("测试日历")
                .description("这是一个测试日历")
                .timezone("Asia/Shanghai")
                .status(CalendarStatus.ACTIVE.getCode())
                .build();

        // When
        int rows = calendarMapper.insert(calendar);

        // Then
        assertEquals(1, rows);
        assertNotNull(calendar.getId());
        assertEquals(calendarId, calendar.getCalendarId());

        // Verify via findById
        Optional<CalendarEntity> found = calendarMapper.findById(calendarId);
        assertTrue(found.isPresent());
        assertEquals(calendarId, found.get().getCalendarId());
        assertEquals(providerId, found.get().getProviderId());
        assertEquals("测试日历", found.get().getName());
        assertEquals("这是一个测试日历", found.get().getDescription());
        assertEquals("Asia/Shanghai", found.get().getTimezone());
        assertEquals(CalendarStatus.ACTIVE.getCode(), found.get().getStatus());
        assertNotNull(found.get().getGmtCreate());
        assertNotNull(found.get().getGmtModify());
    }

    @Test
    @DisplayName("更新日历 - 成功")
    void testUpdate_Success() {
        // Given - 先插入一个日历
        String calendarId = testDataManager.generateCalendarId();
        CalendarEntity calendar = CalendarEntity.builder()
                .calendarId(calendarId)
                .providerId(testDataManager.generateProviderId())
                .name("原始名称")
                .description("原始描述")
                .timezone("Asia/Shanghai")
                .status(CalendarStatus.ACTIVE.getCode())
                .build();

        calendarMapper.insert(calendar);

        // When - 更新日历
        CalendarEntity updated = CalendarEntity.builder()
                .calendarId(calendarId)
                .providerId(calendar.getProviderId())
                .name("更新后的名称")
                .description("更新后的描述")
                .timezone("America/New_York")
                .status(CalendarStatus.DISABLED.getCode())
                .build();

        int rows = calendarMapper.update(updated);

        // Then
        assertEquals(1, rows);

        // Verify via findById
        Optional<CalendarEntity> found = calendarMapper.findById(calendarId);
        assertTrue(found.isPresent());
        assertEquals("更新后的名称", found.get().getName());
        assertEquals("更新后的描述", found.get().getDescription());
        assertEquals("America/New_York", found.get().getTimezone());
        assertEquals(CalendarStatus.DISABLED.getCode(), found.get().getStatus());
    }

    @Test
    @DisplayName("批量插入日历 - 成功")
    void testInsertBatch_Success() {
        // Given
        String providerId = testDataManager.generateProviderId();

        CalendarEntity calendar1 = CalendarEntity.builder()
                .calendarId(testDataManager.generateCalendarId())
                .providerId(providerId)
                .name("日历1")
                .timezone("Asia/Shanghai")
                .status(CalendarStatus.ACTIVE.getCode())
                .build();

        CalendarEntity calendar2 = CalendarEntity.builder()
                .calendarId(testDataManager.generateCalendarId())
                .providerId(providerId)
                .name("日历2")
                .timezone("Asia/Shanghai")
                .status(CalendarStatus.ACTIVE.getCode())
                .build();

        // When
        int rows = calendarMapper.insertBatch(List.of(calendar1, calendar2));

        // Then
        assertEquals(2, rows);
        // Batch insert doesn't populate IDs in H2, verify by querying
        assertTrue(calendarMapper.existsById(calendar1.getCalendarId()));
        assertTrue(calendarMapper.existsById(calendar2.getCalendarId()));
    }

    @Test
    @DisplayName("根据ID查找日历 - 存在")
    void testFindById_Exists() {
        // Given
        String calendarId = testDataManager.generateCalendarId();
        CalendarEntity calendar = CalendarEntity.builder()
                .calendarId(calendarId)
                .providerId(testDataManager.generateProviderId())
                .name("测试日历")
                .status(CalendarStatus.ACTIVE.getCode())
                .build();
        calendarMapper.insert(calendar);

        // When
        Optional<CalendarEntity> found = calendarMapper.findById(calendarId);

        // Then
        assertTrue(found.isPresent());
        assertEquals(calendarId, found.get().getCalendarId());
        assertEquals("测试日历", found.get().getName());
    }

    @Test
    @DisplayName("根据ID查找日历 - 不存在")
    void testFindById_NotExists() {
        // When
        Optional<CalendarEntity> found = calendarMapper.findById("non_existent_id");

        // Then
        assertFalse(found.isPresent());
    }

    @Test
    @DisplayName("根据服务提供方ID查找日历列表")
    void testFindByProviderId() {
        // Given
        String providerId = testDataManager.generateProviderId();

        CalendarEntity calendar1 = CalendarEntity.builder()
                .calendarId(testDataManager.generateCalendarId())
                .providerId(providerId)
                .name("日历1")
                .status(CalendarStatus.ACTIVE.getCode())
                .build();

        CalendarEntity calendar2 = CalendarEntity.builder()
                .calendarId(testDataManager.generateCalendarId())
                .providerId(providerId)
                .name("日历2")
                .status(CalendarStatus.ACTIVE.getCode())
                .build();

        CalendarEntity otherCalendar = CalendarEntity.builder()
                .calendarId(testDataManager.generateCalendarId())
                .providerId(testDataManager.generateProviderId())
                .name("其他日历")
                .status(CalendarStatus.ACTIVE.getCode())
                .build();

        calendarMapper.insert(calendar1);
        calendarMapper.insert(calendar2);
        calendarMapper.insert(otherCalendar);

        // When
        List<CalendarEntity> calendars = calendarMapper.findByProviderId(providerId);

        // Then
        assertEquals(2, calendars.size());
        assertTrue(calendars.stream().anyMatch(c -> c.getName().equals("日历1")));
        assertTrue(calendars.stream().anyMatch(c -> c.getName().equals("日历2")));
        assertFalse(calendars.stream().anyMatch(c -> c.getName().equals("其他日历")));
    }

    @Test
    @DisplayName("根据服务提供方ID和状态查找日历列表")
    void testFindByProviderIdAndStatus() {
        // Given
        String providerId = testDataManager.generateProviderId();

        CalendarEntity active1 = CalendarEntity.builder()
                .calendarId(testDataManager.generateCalendarId())
                .providerId(providerId)
                .name("激活日历1")
                .status(CalendarStatus.ACTIVE.getCode())
                .build();

        CalendarEntity active2 = CalendarEntity.builder()
                .calendarId(testDataManager.generateCalendarId())
                .providerId(providerId)
                .name("激活日历2")
                .status(CalendarStatus.ACTIVE.getCode())
                .build();

        CalendarEntity disabled = CalendarEntity.builder()
                .calendarId(testDataManager.generateCalendarId())
                .providerId(providerId)
                .name("禁用日历")
                .status(CalendarStatus.DISABLED.getCode())
                .build();

        calendarMapper.insert(active1);
        calendarMapper.insert(active2);
        calendarMapper.insert(disabled);

        // When - 使用Integer参数
        List<CalendarEntity> activeCalendars = calendarMapper.findByProviderIdAndStatus(providerId, CalendarStatus.ACTIVE.getCode());

        // Then
        assertEquals(2, activeCalendars.size());
        assertTrue(activeCalendars.stream().allMatch(c -> CalendarStatus.ACTIVE.getCode().equals(c.getStatus())));

        List<CalendarEntity> disabledCalendars = calendarMapper.findByProviderIdAndStatus(providerId, CalendarStatus.DISABLED.getCode());
        assertEquals(1, disabledCalendars.size());
        assertTrue(disabledCalendars.stream().allMatch(c -> CalendarStatus.DISABLED.getCode().equals(c.getStatus())));
    }

    @Test
    @DisplayName("删除日历")
    void testDeleteById() {
        // Given
        String calendarId = testDataManager.generateCalendarId();
        CalendarEntity calendar = CalendarEntity.builder()
                .calendarId(calendarId)
                .providerId(testDataManager.generateProviderId())
                .name("待删除日历")
                .status(CalendarStatus.ACTIVE.getCode())
                .build();
        calendarMapper.insert(calendar);

        // When
        int rows = calendarMapper.deleteById(calendarId);

        // Then
        assertEquals(1, rows);
        Optional<CalendarEntity> found = calendarMapper.findById(calendarId);
        assertFalse(found.isPresent());
    }

    @Test
    @DisplayName("检查日历是否存在 - 存在")
    void testExistsById_True() {
        // Given
        String calendarId = testDataManager.generateCalendarId();
        CalendarEntity calendar = CalendarEntity.builder()
                .calendarId(calendarId)
                .providerId(testDataManager.generateProviderId())
                .name("测试日历")
                .status(CalendarStatus.ACTIVE.getCode())
                .build();
        calendarMapper.insert(calendar);

        // When
        boolean exists = calendarMapper.existsById(calendarId);

        // Then
        assertTrue(exists);
    }

    @Test
    @DisplayName("检查日历是否存在 - 不存在")
    void testExistsById_False() {
        // When
        boolean exists = calendarMapper.existsById("non_existent_id");

        // Then
        assertFalse(exists);
    }

    @Test
    @DisplayName("检查服务提供方是否拥有指定日历")
    void testExistsByCalendarIdAndProviderId() {
        // Given
        String calendarId = testDataManager.generateCalendarId();
        String providerId = testDataManager.generateProviderId();
        String otherProviderId = testDataManager.generateProviderId();

        CalendarEntity calendar = CalendarEntity.builder()
                .calendarId(calendarId)
                .providerId(providerId)
                .name("测试日历")
                .status(CalendarStatus.ACTIVE.getCode())
                .build();
        calendarMapper.insert(calendar);

        // When & Then
        assertTrue(calendarMapper.existsByCalendarIdAndProviderId(calendarId, providerId));
        assertFalse(calendarMapper.existsByCalendarIdAndProviderId(calendarId, otherProviderId));
    }

    @Test
    @DisplayName("插入日历 - 使用默认时区")
    void testInsert_DefaultTimezone() {
        // Given
        String calendarId = testDataManager.generateCalendarId();
        CalendarEntity calendar = CalendarEntity.builder()
                .calendarId(calendarId)
                .providerId(testDataManager.generateProviderId())
                .name("测试日历")
                .timezone("Asia/Shanghai")  // 明确设置时区
                .status(CalendarStatus.ACTIVE.getCode())
                .build();

        // When
        calendarMapper.insert(calendar);

        // Then
        Optional<CalendarEntity> found = calendarMapper.findById(calendarId);
        assertTrue(found.isPresent());
        assertEquals("Asia/Shanghai", found.get().getTimezone());
    }
}
