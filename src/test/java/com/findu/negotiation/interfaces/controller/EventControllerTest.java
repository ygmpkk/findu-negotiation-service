package com.findu.negotiation.interfaces.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.findu.negotiation.application.CalendarBizService;
import com.findu.negotiation.domain.calendar.entity.EventEntity;
import com.findu.negotiation.domain.calendar.enums.EventStatus;
import com.findu.negotiation.domain.calendar.enums.EventType;
import com.findu.negotiation.domain.calendar.enums.Visibility;
import com.findu.negotiation.domain.calendar.enums.FreeBusyStatus;
import com.findu.negotiation.domain.calendar.vo.LocationVO;
import com.findu.negotiation.domain.calendar.vo.ReminderVO;
import com.findu.negotiation.domain.calendar.vo.TimeInfoVO;
import com.findu.negotiation.infrastructure.exception.BusinessException;
import com.findu.negotiation.infrastructure.exception.ErrorCode;
import com.findu.negotiation.infrastructure.handler.GlobalExceptionHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * EventController 单元测试
 *
 * @author timothy
 * @date 2026/01/25
 */
@WebMvcTest(EventController.class)
@ActiveProfiles("test")
@Import(GlobalExceptionHandler.class)
@DisplayName("EventController 单元测试")
class EventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CalendarBizService calendarBizService;

    @Test
    @DisplayName("创建日程 - 成功")
    void testCreate_Success() throws Exception {
        // Given
        String requestJson = """
                {
                    "calendarId": "cal_123",
                    "title": "测试日程",
                    "description": "这是一个测试日程",
                    "visibility": 1,
                    "startTime": {
                        "timestamp": "2026-01-25T10:00:00",
                        "timezone": "Asia/Shanghai"
                    },
                    "endTime": {
                        "timestamp": "2026-01-25T11:00:00",
                        "timezone": "Asia/Shanghai"
                    },
                    "eventType": 0,
                    "reminders": [{"minutes": 15}],
                    "location": {
                        "name": "会议室",
                        "address": "上海市浦东新区"
                    }
                }
                """;

        EventEntity event = EventEntity.builder()
                .eventId("evt_123")
                .calendarId("cal_123")
                .title("测试日程")
                .description("这是一个测试日程")
                .visibility(Visibility.PRIVATE.getCode())
                .startTime(TimeInfoVO.forSpecificTime("2026-01-25T10:00:00", "Asia/Shanghai"))
                .endTime(TimeInfoVO.forSpecificTime("2026-01-25T11:00:00", "Asia/Shanghai"))
                .eventType(EventType.SINGLE.getCode())
                .status(EventStatus.SCHEDULED.getCode())
                .freeBusyStatus(FreeBusyStatus.BUSY.getCode())
                .reminderVOS(List.of(ReminderVO.builder().minutes(15).build()))
                .locationVO(LocationVO.builder().name("会议室").address("上海市浦东新区").build())
                .build();

        when(calendarBizService.createEvent(any(EventEntity.class)))
                .thenReturn(event);

        // When & Then
        mockMvc.perform(post("/api/v1/calendars/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.eventId").value("evt_123"))
                .andExpect(jsonPath("$.data.title").value("测试日程"))
                .andExpect(jsonPath("$.data.status").value(EventStatus.SCHEDULED.getCode()));

        verify(calendarBizService, times(1)).createEvent(any(EventEntity.class));
    }

    @Test
    @DisplayName("创建日程 - 业务异常")
    void testCreate_BusinessException() throws Exception {
        // Given
        String requestJson = """
                {
                    "calendarId": "cal_123",
                    "title": "测试日程",
                    "startTime": {
                        "timestamp": "2026-01-25T10:00:00",
                        "timezone": "Asia/Shanghai"
                    },
                    "endTime": {
                        "timestamp": "2026-01-25T11:00:00",
                        "timezone": "Asia/Shanghai"
                    },
                    "eventType": 0
                }
                """;

        when(calendarBizService.createEvent(any(EventEntity.class)))
                .thenThrow(new BusinessException(ErrorCode.NOT_FOUND, "日历不存在"));

        // When & Then
        mockMvc.perform(post("/api/v1/calendars/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ErrorCode.NOT_FOUND.getCode()))
                .andExpect(jsonPath("$.message").value("日历不存在"));
    }

    @Test
    @DisplayName("更新日程 - 成功")
    void testUpdate_Success() throws Exception {
        // Given
        String eventId = "evt_123";
        String requestJson = """
                {
                    "eventId": "evt_123",
                    "title": "更新后的日程",
                    "description": "更新后的描述",
                    "startTime": {
                        "timestamp": "2026-01-25T14:00:00",
                        "timezone": "Asia/Shanghai"
                    },
                    "endTime": {
                        "timestamp": "2026-01-25T15:00:00",
                        "timezone": "Asia/Shanghai"
                    },
                    "status": 2
                }
                """;

        EventEntity existing = EventEntity.builder()
                .eventId(eventId)
                .calendarId("cal_123")
                .title("原始标题")
                .description("原始描述")
                .startTime(TimeInfoVO.forSpecificTime("2026-01-25T10:00:00", "Asia/Shanghai"))
                .endTime(TimeInfoVO.forSpecificTime("2026-01-25T11:00:00", "Asia/Shanghai"))
                .eventType(EventType.SINGLE.getCode())
                .status(EventStatus.SCHEDULED.getCode())
                .freeBusyStatus(FreeBusyStatus.BUSY.getCode())
                .reminderVOS(List.of())
                .locationVO(null)
                .build();

        EventEntity updated = EventEntity.builder()
                .eventId(eventId)
                .calendarId("cal_123")
                .title("更新后的日程")
                .description("更新后的描述")
                .startTime(TimeInfoVO.forSpecificTime("2026-01-25T14:00:00", "Asia/Shanghai"))
                .endTime(TimeInfoVO.forSpecificTime("2026-01-25T15:00:00", "Asia/Shanghai"))
                .eventType(EventType.SINGLE.getCode())
                .status(EventStatus.CANCELLED.getCode())
                .freeBusyStatus(FreeBusyStatus.BUSY.getCode())
                .reminderVOS(List.of())
                .locationVO(null)
                .build();

        when(calendarBizService.getEvent(eventId)).thenReturn(existing);
        when(calendarBizService.updateEvent(any(EventEntity.class)))
                .thenReturn(updated);

        // When & Then
        mockMvc.perform(put("/api/v1/calendars/events/" + eventId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.eventId").value(eventId))
                .andExpect(jsonPath("$.data.title").value("更新后的日程"))
                .andExpect(jsonPath("$.data.status").value(EventStatus.CANCELLED.getCode()));
    }

    @Test
    @DisplayName("更新日程 - 日程不存在")
    void testUpdate_NotFound() throws Exception {
        // Given
        String eventId = "evt_123";
        String requestJson = """
                {
                    "eventId": "evt_123",
                    "title": "更新后的日程"
                }
                """;

        when(calendarBizService.getEvent(eventId)).thenReturn(null);

        // When & Then
        mockMvc.perform(put("/api/v1/calendars/events/" + eventId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ErrorCode.NOT_FOUND.getCode()))
                .andExpect(jsonPath("$.message").value("日程不存在"));
    }

    @Test
    @DisplayName("删除日程 - 成功")
    void testDelete_Success() throws Exception {
        // Given
        String eventId = "evt_123";
        doNothing().when(calendarBizService).deleteEvent(eventId);

        // When & Then
        mockMvc.perform(delete("/api/v1/calendars/events/" + eventId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(calendarBizService, times(1)).deleteEvent(eventId);
    }

    @Test
    @DisplayName("获取日程详情 - 成功")
    void testGet_Success() throws Exception {
        // Given
        String eventId = "evt_123";
        EventEntity event = EventEntity.builder()
                .eventId(eventId)
                .calendarId("cal_123")
                .title("测试日程")
                .description("测试描述")
                .startTime(TimeInfoVO.forSpecificTime("2026-01-25T10:00:00", "Asia/Shanghai"))
                .endTime(TimeInfoVO.forSpecificTime("2026-01-25T11:00:00", "Asia/Shanghai"))
                .eventType(EventType.SINGLE.getCode())
                .status(EventStatus.SCHEDULED.getCode())
                .freeBusyStatus(FreeBusyStatus.BUSY.getCode())
                .reminderVOS(List.of())
                .locationVO(null)
                .build();

        when(calendarBizService.getEvent(eventId)).thenReturn(event);

        // When & Then
        mockMvc.perform(get("/api/v1/calendars/events/" + eventId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.eventId").value(eventId))
                .andExpect(jsonPath("$.data.title").value("测试日程"));
    }

    @Test
    @DisplayName("获取日程详情 - 不存在")
    void testGet_NotFound() throws Exception {
        // Given
        String eventId = "evt_123";
        when(calendarBizService.getEvent(eventId)).thenReturn(null);

        // When & Then
        mockMvc.perform(get("/api/v1/calendars/events/" + eventId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ErrorCode.NOT_FOUND.getCode()))
                .andExpect(jsonPath("$.message").value("日程不存在"));
    }

    @Test
    @DisplayName("获取日历中的所有日程 - 成功")
    void testListByCalendar_Success() throws Exception {
        // Given
        String calendarId = "cal_123";
        List<EventEntity> events = List.of(
                EventEntity.builder()
                        .eventId("evt_1")
                        .calendarId(calendarId)
                        .title("日程1")
                        .startTime(TimeInfoVO.forSpecificTime("2026-01-25T10:00:00", "Asia/Shanghai"))
                        .endTime(TimeInfoVO.forSpecificTime("2026-01-25T11:00:00", "Asia/Shanghai"))
                        .eventType(EventType.SINGLE.getCode())
                        .status(EventStatus.SCHEDULED.getCode())
                        .freeBusyStatus(FreeBusyStatus.BUSY.getCode())
                        .reminderVOS(List.of())
                        .locationVO(null)
                        .build(),
                EventEntity.builder()
                        .eventId("evt_2")
                        .calendarId(calendarId)
                        .title("日程2")
                        .startTime(TimeInfoVO.forSpecificTime("2026-01-25T14:00:00", "Asia/Shanghai"))
                        .endTime(TimeInfoVO.forSpecificTime("2026-01-25T15:00:00", "Asia/Shanghai"))
                        .eventType(EventType.SINGLE.getCode())
                        .status(EventStatus.SCHEDULED.getCode())
                        .freeBusyStatus(FreeBusyStatus.BUSY.getCode())
                        .reminderVOS(List.of())
                        .locationVO(null)
                        .build()
        );

        when(calendarBizService.getEventsByCalendar(calendarId)).thenReturn(events);

        // When & Then
        mockMvc.perform(get("/api/v1/calendars/events/calendar/" + calendarId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].eventId").value("evt_1"))
                .andExpect(jsonPath("$.data[1].eventId").value("evt_2"));
    }

    @Test
    @DisplayName("创建日程 - 缺少必填字段")
    void testCreate_MissingRequiredFields() throws Exception {
        // Given - 缺少必填字段 calendarId 和 title
        String requestJson = """
                {
                    "startTime": {
                        "timestamp": "2026-01-25T10:00:00",
                        "timezone": "Asia/Shanghai"
                    },
                    "endTime": {
                        "timestamp": "2026-01-25T11:00:00",
                        "timezone": "Asia/Shanghai"
                    },
                    "eventType": 0
                }
                """;

        // When & Then
        mockMvc.perform(post("/api/v1/calendars/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ErrorCode.PARAM_ERROR.getCode()));
    }
}
