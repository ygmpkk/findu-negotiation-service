package com.findu.negotiation.interfaces.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.findu.negotiation.application.CalendarBizService;
import com.findu.negotiation.domain.calendar.entity.CalendarEntity;
import com.findu.negotiation.domain.calendar.enums.CalendarStatus;
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
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * CalendarController 单元测试
 *
 * @author timothy
 * @date 2026/01/25
 */
@WebMvcTest(CalendarController.class)
@ActiveProfiles("test")
@Import(GlobalExceptionHandler.class)
@DisplayName("CalendarController 单元测试")
class CalendarControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CalendarBizService calendarBizService;

    @Test
    @DisplayName("创建日历 - 成功")
    void testCreate_Success() throws Exception {
        // Given
        String requestJson = """
                {
                    "providerId": "provider_123",
                    "name": "测试日历",
                    "description": "这是一个测试日历",
                    "timezone": "Asia/Shanghai"
                }
                """;

        CalendarEntity calendar = CalendarEntity.builder()
                .calendarId("cal_123")
                .providerId("provider_123")
                .name("测试日历")
                .description("这是一个测试日历")
                .timezone("Asia/Shanghai")
                .status(CalendarStatus.ACTIVE.getCode())
                .build();

        when(calendarBizService.createCalendar(any(CalendarEntity.class)))
                .thenReturn(calendar);

        // When & Then
        mockMvc.perform(post("/api/v1/calendars")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.calendarId").value("cal_123"))
                .andExpect(jsonPath("$.data.name").value("测试日历"))
                .andExpect(jsonPath("$.data.description").value("这是一个测试日历"))
                .andExpect(jsonPath("$.data.timezone").value("Asia/Shanghai"))
                .andExpect(jsonPath("$.data.status").value(CalendarStatus.ACTIVE.getCode()));

        verify(calendarBizService, times(1)).createCalendar(any(CalendarEntity.class));
    }

    @Test
    @DisplayName("创建日历 - 使用默认时区")
    void testCreate_WithDefaultTimezone() throws Exception {
        // Given
        String requestJson = """
                {
                    "providerId": "provider_123",
                    "name": "测试日历"
                }
                """;

        CalendarEntity calendar = CalendarEntity.builder()
                .calendarId("cal_123")
                .providerId("provider_123")
                .name("测试日历")
                .timezone("Asia/Shanghai")
                .status(CalendarStatus.ACTIVE.getCode())
                .build();

        when(calendarBizService.createCalendar(any(CalendarEntity.class)))
                .thenReturn(calendar);

        // When & Then
        mockMvc.perform(post("/api/v1/calendars")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.timezone").value("Asia/Shanghai"));
    }

    @Test
    @DisplayName("创建日历 - 业务异常")
    void testCreate_BusinessException() throws Exception {
        // Given
        String requestJson = """
                {
                    "providerId": "provider_123",
                    "name": "测试日历"
                }
                """;

        when(calendarBizService.createCalendar(any(CalendarEntity.class)))
                .thenThrow(new BusinessException(ErrorCode.INTERNAL_ERROR, "日历已存在"));

        // When & Then
        mockMvc.perform(post("/api/v1/calendars")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ErrorCode.INTERNAL_ERROR.getCode()))
                .andExpect(jsonPath("$.message").value("日历已存在"));
    }

    @Test
    @DisplayName("更新日历 - 成功")
    void testUpdate_Success() throws Exception {
        // Given
        String calendarId = "cal_123";
        String requestJson = """
                {
                    "calendarId": "cal_123",
                    "name": "更新后的日历",
                    "description": "更新后的描述",
                    "timezone": "America/New_York",
                    "status": 1
                }
                """;

        CalendarEntity updated = CalendarEntity.builder()
                .calendarId(calendarId)
                .providerId("provider_123")
                .name("更新后的日历")
                .description("更新后的描述")
                .timezone("America/New_York")
                .status(CalendarStatus.DISABLED.getCode())
                .build();

        when(calendarBizService.updateCalendar(any(CalendarEntity.class)))
                .thenReturn(updated);

        // When & Then
        mockMvc.perform(put("/api/v1/calendars/" + calendarId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.calendarId").value(calendarId))
                .andExpect(jsonPath("$.data.name").value("更新后的日历"))
                .andExpect(jsonPath("$.data.description").value("更新后的描述"))
                .andExpect(jsonPath("$.data.timezone").value("America/New_York"))
                .andExpect(jsonPath("$.data.status").value(CalendarStatus.DISABLED.getCode()));
    }

    @Test
    @DisplayName("删除日历 - 成功")
    void testDelete_Success() throws Exception {
        // Given
        String calendarId = "cal_123";
        doNothing().when(calendarBizService).deleteCalendar(calendarId);

        // When & Then
        mockMvc.perform(delete("/api/v1/calendars/" + calendarId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(calendarBizService, times(1)).deleteCalendar(calendarId);
    }

    @Test
    @DisplayName("删除日历 - 业务异常")
    void testDelete_BusinessException() throws Exception {
        // Given
        String calendarId = "cal_123";
        doThrow(new BusinessException(ErrorCode.NOT_FOUND, "日历不存在"))
                .when(calendarBizService).deleteCalendar(calendarId);

        // When & Then
        mockMvc.perform(delete("/api/v1/calendars/" + calendarId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ErrorCode.NOT_FOUND.getCode()))
                .andExpect(jsonPath("$.message").value("日历不存在"));
    }

    @Test
    @DisplayName("获取日历详情 - 成功")
    void testGet_Success() throws Exception {
        // Given
        String calendarId = "cal_123";
        CalendarEntity calendar = CalendarEntity.builder()
                .calendarId(calendarId)
                .providerId("provider_123")
                .name("测试日历")
                .description("测试描述")
                .timezone("Asia/Shanghai")
                .status(CalendarStatus.ACTIVE.getCode())
                .build();

        when(calendarBizService.getCalendar(calendarId)).thenReturn(calendar);

        // When & Then
        mockMvc.perform(get("/api/v1/calendars/" + calendarId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.calendarId").value(calendarId))
                .andExpect(jsonPath("$.data.name").value("测试日历"));
    }

    @Test
    @DisplayName("获取日历详情 - 不存在")
    void testGet_NotFound() throws Exception {
        // Given
        String calendarId = "cal_123";
        when(calendarBizService.getCalendar(calendarId)).thenReturn(null);

        // When & Then
        mockMvc.perform(get("/api/v1/calendars/" + calendarId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ErrorCode.NOT_FOUND.getCode()))
                .andExpect(jsonPath("$.message").value("日历不存在"));
    }

    @Test
    @DisplayName("获取服务提供方日历列表 - 成功")
    void testListByProvider_Success() throws Exception {
        // Given
        String providerId = "provider_123";
        List<CalendarEntity> calendars = List.of(
                CalendarEntity.builder()
                        .calendarId("cal_1")
                        .providerId(providerId)
                        .name("日历1")
                        .timezone("Asia/Shanghai")
                        .status(CalendarStatus.ACTIVE.getCode())
                        .build(),
                CalendarEntity.builder()
                        .calendarId("cal_2")
                        .providerId(providerId)
                        .name("日历2")
                        .timezone("Asia/Shanghai")
                        .status(CalendarStatus.ACTIVE.getCode())
                        .build()
        );

        when(calendarBizService.getCalendarsByProvider(providerId)).thenReturn(calendars);

        // When & Then
        mockMvc.perform(get("/api/v1/calendars/provider/" + providerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].calendarId").value("cal_1"))
                .andExpect(jsonPath("$.data[1].calendarId").value("cal_2"));
    }

    @Test
    @DisplayName("获取服务提供方激活日历列表 - 成功")
    void testListActiveByProvider_Success() throws Exception {
        // Given
        String providerId = "provider_123";
        List<CalendarEntity> activeCalendars = List.of(
                CalendarEntity.builder()
                        .calendarId("cal_1")
                        .providerId(providerId)
                        .name("激活日历")
                        .timezone("Asia/Shanghai")
                        .status(CalendarStatus.ACTIVE.getCode())
                        .build()
        );

        when(calendarBizService.getActiveCalendars(providerId)).thenReturn(activeCalendars);

        // When & Then
        mockMvc.perform(get("/api/v1/calendars/provider/" + providerId + "/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].status").value(CalendarStatus.ACTIVE.getCode()));
    }

    @Test
    @DisplayName("创建日历 - 参数校验失败")
    void testCreate_ValidationError() throws Exception {
        // Given - 缺少必填字段 providerId
        String requestJson = """
                {
                    "name": "测试日历"
                }
                """;

        // When & Then
        mockMvc.perform(post("/api/v1/calendars")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ErrorCode.PARAM_ERROR.getCode()));
    }

    @Test
    @DisplayName("更新日历 - 参数校验失败")
    void testUpdate_ValidationError() throws Exception {
        // Given - 缺少必填字段 name (calendarId is required but empty string would pass @NotBlank)
        String requestJson = """
                {
                    "calendarId": "",
                    "description": "描述"
                }
                """;

        // When & Then
        mockMvc.perform(put("/api/v1/calendars/cal_123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ErrorCode.PARAM_ERROR.getCode()));
    }
}
