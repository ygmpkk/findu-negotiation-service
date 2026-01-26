package com.findu.negotiation.interfaces.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.findu.negotiation.application.CalendarBizService;
import com.findu.negotiation.domain.calendar.entity.AttendeeEntity;
import com.findu.negotiation.domain.calendar.enums.AttendeeRole;
import com.findu.negotiation.domain.calendar.enums.RsvpStatus;
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
 * AttendeeController 单元测试
 *
 * @author timothy
 * @date 2026/01/25
 */
@WebMvcTest(AttendeeController.class)
@ActiveProfiles("test")
@Import(GlobalExceptionHandler.class)
@DisplayName("AttendeeController 单元测试")
class AttendeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CalendarBizService calendarBizService;

    @Test
    @DisplayName("添加参与人 - 成功")
    void testCreate_Success() throws Exception {
        // Given
        String requestJson = """
                {
                    "eventId": "evt_123",
                    "customerId": "customer_123",
                    "role": 0,
                    "rsvpStatus": 0
                }
                """;

        AttendeeEntity attendee = AttendeeEntity.builder()
                .attendeeId("att_123")
                .eventId("evt_123")
                .customerId("customer_123")
                .role(AttendeeRole.REQUIRED.getCode())
                .rsvpStatus(RsvpStatus.PENDING.getCode())
                .build();

        when(calendarBizService.addAttendee(eq("evt_123"), any(AttendeeEntity.class)))
                .thenReturn(attendee);

        // When & Then
        mockMvc.perform(post("/api/v1/calendars/attendees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.attendeeId").value("att_123"))
                .andExpect(jsonPath("$.data.eventId").value("evt_123"))
                .andExpect(jsonPath("$.data.customerId").value("customer_123"))
                .andExpect(jsonPath("$.data.role").value(AttendeeRole.REQUIRED.getCode()))
                .andExpect(jsonPath("$.data.rsvpStatus").value(RsvpStatus.PENDING.getCode()));

        verify(calendarBizService, times(1)).addAttendee(eq("evt_123"), any(AttendeeEntity.class));
    }

    @Test
    @DisplayName("添加参与人 - 使用默认值")
    void testCreate_WithDefaults() throws Exception {
        // Given
        String requestJson = """
                {
                    "eventId": "evt_123",
                    "customerId": "customer_123"
                }
                """;

        AttendeeEntity attendee = AttendeeEntity.builder()
                .attendeeId("att_123")
                .eventId("evt_123")
                .customerId("customer_123")
                .role(AttendeeRole.REQUIRED.getCode())  // 默认值
                .rsvpStatus(RsvpStatus.PENDING.getCode())  // 默认值
                .build();

        when(calendarBizService.addAttendee(eq("evt_123"), any(AttendeeEntity.class)))
                .thenReturn(attendee);

        // When & Then
        mockMvc.perform(post("/api/v1/calendars/attendees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.role").value(AttendeeRole.REQUIRED.getCode()))
                .andExpect(jsonPath("$.data.rsvpStatus").value(RsvpStatus.PENDING.getCode()));
    }

    @Test
    @DisplayName("添加参与人 - 业务异常")
    void testCreate_BusinessException() throws Exception {
        // Given
        String requestJson = """
                {
                    "eventId": "evt_123",
                    "customerId": "customer_123"
                }
                """;

        when(calendarBizService.addAttendee(eq("evt_123"), any(AttendeeEntity.class)))
                .thenThrow(new BusinessException(ErrorCode.NOT_FOUND, "日程不存在"));

        // When & Then
        mockMvc.perform(post("/api/v1/calendars/attendees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ErrorCode.NOT_FOUND.getCode()))
                .andExpect(jsonPath("$.message").value("日程不存在"));
    }

    @Test
    @DisplayName("更新参与人 - 成功")
    void testUpdate_Success() throws Exception {
        // Given
        String attendeeId = "att_123";
        String requestJson = """
                {
                    "attendeeId": "att_123",
                    "rsvpStatus": 1
                }
                """;

        doNothing().when(calendarBizService).updateRsvpStatus(attendeeId, RsvpStatus.ACCEPTED.getCode());

        // When & Then
        mockMvc.perform(put("/api/v1/calendars/attendees/" + attendeeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(calendarBizService, times(1)).updateRsvpStatus(attendeeId, RsvpStatus.ACCEPTED.getCode());
    }

    @Test
    @DisplayName("更新参与人 - 不做任何更新")
    void testUpdate_NoUpdate() throws Exception {
        // Given
        String attendeeId = "att_123";
        String requestJson = """
                {
                    "attendeeId": "att_123",
                    "rsvpStatus": null
                }
                """;

        // When & Then
        mockMvc.perform(put("/api/v1/calendars/attendees/" + attendeeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(calendarBizService, never()).updateRsvpStatus(any(), any());
    }

    @Test
    @DisplayName("移除参与人 - 成功")
    void testDelete_Success() throws Exception {
        // Given
        String attendeeId = "att_123";
        String eventId = "evt_123";
        doNothing().when(calendarBizService).removeAttendee(eventId, attendeeId);

        // When & Then
        mockMvc.perform(delete("/api/v1/calendars/attendees/" + attendeeId)
                        .param("eventId", eventId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(calendarBizService, times(1)).removeAttendee(eventId, attendeeId);
    }

    @Test
    @DisplayName("移除参与人 - 业务异常")
    void testDelete_BusinessException() throws Exception {
        // Given
        String attendeeId = "att_123";
        String eventId = "evt_123";
        doThrow(new BusinessException(ErrorCode.NOT_FOUND, "参与人不存在"))
                .when(calendarBizService).removeAttendee(eventId, attendeeId);

        // When & Then
        mockMvc.perform(delete("/api/v1/calendars/attendees/" + attendeeId)
                        .param("eventId", eventId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ErrorCode.NOT_FOUND.getCode()))
                .andExpect(jsonPath("$.message").value("参与人不存在"));
    }

    @Test
    @DisplayName("获取参与人详情 - 成功")
    void testGet_Success() throws Exception {
        // Given
        String attendeeId = "att_123";
        AttendeeEntity attendee = AttendeeEntity.builder()
                .attendeeId(attendeeId)
                .eventId("evt_123")
                .customerId("customer_123")
                .role(AttendeeRole.REQUIRED.getCode())
                .rsvpStatus(RsvpStatus.ACCEPTED.getCode())
                .build();

        when(calendarBizService.getAttendee(attendeeId)).thenReturn(attendee);

        // When & Then
        mockMvc.perform(get("/api/v1/calendars/attendees/" + attendeeId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.attendeeId").value(attendeeId))
                .andExpect(jsonPath("$.data.customerId").value("customer_123"))
                .andExpect(jsonPath("$.data.rsvpStatus").value(RsvpStatus.ACCEPTED.getCode()));
    }

    @Test
    @DisplayName("获取参与人详情 - 不存在")
    void testGet_NotFound() throws Exception {
        // Given
        String attendeeId = "att_123";
        when(calendarBizService.getAttendee(attendeeId)).thenReturn(null);

        // When & Then
        mockMvc.perform(get("/api/v1/calendars/attendees/" + attendeeId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ErrorCode.NOT_FOUND.getCode()))
                .andExpect(jsonPath("$.message").value("参与人不存在"));
    }

    @Test
    @DisplayName("获取日程的所有参与人 - 成功")
    void testListByEvent_Success() throws Exception {
        // Given
        String eventId = "evt_123";
        List<AttendeeEntity> attendees = List.of(
                AttendeeEntity.builder()
                        .attendeeId("att_1")
                        .eventId(eventId)
                        .customerId("customer_1")
                        .role(AttendeeRole.REQUIRED.getCode())
                        .rsvpStatus(RsvpStatus.ACCEPTED.getCode())
                        .build(),
                AttendeeEntity.builder()
                        .attendeeId("att_2")
                        .eventId(eventId)
                        .customerId("customer_2")
                        .role(AttendeeRole.OPTIONAL.getCode())
                        .rsvpStatus(RsvpStatus.PENDING.getCode())
                        .build()
        );

        when(calendarBizService.getAttendees(eventId)).thenReturn(attendees);

        // When & Then
        mockMvc.perform(get("/api/v1/calendars/attendees/event/" + eventId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].attendeeId").value("att_1"))
                .andExpect(jsonPath("$.data[1].attendeeId").value("att_2"));
    }

    @Test
    @DisplayName("更新参与人 RSVP 状态 - 成功")
    void testUpdateRsvp_Success() throws Exception {
        // Given
        String attendeeId = "att_123";
        Integer status = RsvpStatus.ACCEPTED.getCode();
        doNothing().when(calendarBizService).updateRsvpStatus(attendeeId, status);

        // When & Then
        mockMvc.perform(patch("/api/v1/calendars/attendees/" + attendeeId + "/rsvp")
                        .param("status", String.valueOf(status)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(calendarBizService, times(1)).updateRsvpStatus(attendeeId, status);
    }

    @Test
    @DisplayName("更新参与人 RSVP 状态 - 业务异常")
    void testUpdateRsvp_BusinessException() throws Exception {
        // Given
        String attendeeId = "att_123";
        Integer status = RsvpStatus.ACCEPTED.getCode();
        doThrow(new BusinessException(ErrorCode.NOT_FOUND, "参与人不存在"))
                .when(calendarBizService).updateRsvpStatus(attendeeId, status);

        // When & Then
        mockMvc.perform(patch("/api/v1/calendars/attendees/" + attendeeId + "/rsvp")
                        .param("status", String.valueOf(status)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ErrorCode.NOT_FOUND.getCode()))
                .andExpect(jsonPath("$.message").value("参与人不存在"));
    }

    @Test
    @DisplayName("添加参与人 - 缺少必填字段")
    void testCreate_MissingRequiredFields() throws Exception {
        // Given - 缺少必填字段 eventId
        String requestJson = """
                {
                    "customerId": "customer_123"
                }
                """;

        // When & Then
        mockMvc.perform(post("/api/v1/calendars/attendees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ErrorCode.PARAM_ERROR.getCode()));
    }
}
