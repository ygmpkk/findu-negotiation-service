package com.findu.negotiation.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.findu.negotiation.interfaces.dto.calendar.*;
import com.findu.negotiation.interfaces.dto.mcp.McpTool;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class McpToolService {

    private final CalendarBizService calendarBizService;
    private final ObjectMapper objectMapper;

    public McpToolService(CalendarBizService calendarBizService, ObjectMapper objectMapper) {
        this.calendarBizService = calendarBizService;
        this.objectMapper = objectMapper;
    }

    public List<McpTool> listTools() {
        return List.of(
            buildTool("calendar.get_freebusy", "根据服务方ID查询闲忙时间", freeBusySchema()),
            buildTool("calendar.create_event", "创建服务方日历事件", createEventSchema()),
            buildTool("calendar.update_event", "更新服务方日历事件", updateEventSchema()),
            buildTool("calendar.cancel_event", "取消服务方日历事件", cancelEventSchema()),
            buildTool("calendar.create_calendar", "创建飞书日历", createCalendarSchema()),
            buildTool("calendar.schedule_appointment", "为需求方安排预约日历", scheduleAppointmentSchema())
        );
    }

    public Object callTool(String toolName, Map<String, Object> arguments) {
        return switch (toolName) {
            case "calendar.get_freebusy" -> {
                CalendarFreeBusyRequest request = convert(arguments, CalendarFreeBusyRequest.class);
                yield calendarBizService.getFreeBusy(request);
            }
            case "calendar.create_event" -> {
                CalendarEventCreateRequest request = convert(arguments, CalendarEventCreateRequest.class);
                yield calendarBizService.createEvent(request);
            }
            case "calendar.update_event" -> {
                String eventId = (String) arguments.get("eventId");
                if (eventId == null) {
                    throw new IllegalArgumentException("缺少eventId");
                }
                CalendarEventUpdateRequest request = convert(arguments, CalendarEventUpdateRequest.class);
                yield calendarBizService.updateEvent(eventId, request);
            }
            case "calendar.cancel_event" -> {
                String calendarId = (String) arguments.get("calendarId");
                String eventId = (String) arguments.get("eventId");
                if (eventId == null) {
                    throw new IllegalArgumentException("缺少eventId");
                }
                calendarBizService.cancelEvent(calendarId, eventId);
                yield Map.of("status", "cancelled");
            }
            case "calendar.create_calendar" -> {
                CalendarCreateRequest request = convert(arguments, CalendarCreateRequest.class);
                yield calendarBizService.createCalendar(request);
            }
            case "calendar.schedule_appointment" -> {
                CalendarAppointmentRequest request = convert(arguments, CalendarAppointmentRequest.class);
                yield calendarBizService.scheduleAppointment(request);
            }
            default -> throw new IllegalArgumentException("未知工具: " + toolName);
        };
    }

    private McpTool buildTool(String name, String description, Map<String, Object> schema) {
        return McpTool.builder()
            .name(name)
            .description(description)
            .inputSchema(schema)
            .build();
    }

    private <T> T convert(Map<String, Object> arguments, Class<T> targetClass) {
        return objectMapper.convertValue(arguments, targetClass);
    }

    private Map<String, Object> freeBusySchema() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("providerId", Map.of("type", "string", "description", "服务方用户ID"));
        properties.put("startTime", Map.of("type", "string", "description", "开始时间(RFC3339)"));
        properties.put("endTime", Map.of("type", "string", "description", "结束时间(RFC3339)"));
        properties.put("timezone", Map.of("type", "string", "description", "时区"));
        properties.put("userIdType", Map.of("type", "string", "description", "用户ID类型"));
        return baseSchema(properties, List.of("providerId", "startTime", "endTime"));
    }

    private Map<String, Object> createEventSchema() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("providerId", Map.of("type", "string"));
        properties.put("calendarId", Map.of("type", "string"));
        properties.put("summary", Map.of("type", "string"));
        properties.put("description", Map.of("type", "string"));
        properties.put("startTime", Map.of("type", "string"));
        properties.put("endTime", Map.of("type", "string"));
        properties.put("timezone", Map.of("type", "string"));
        properties.put("attendeeIds", Map.of("type", "array", "items", Map.of("type", "string")));
        properties.put("userIdType", Map.of("type", "string"));
        return baseSchema(properties, List.of("providerId", "summary", "startTime", "endTime"));
    }

    private Map<String, Object> updateEventSchema() {
        Map<String, Object> schema = createEventSchema();
        @SuppressWarnings("unchecked")
        Map<String, Object> properties = (Map<String, Object>) schema.get("properties");
        properties.put("eventId", Map.of("type", "string"));
        schema.put("required", List.of("eventId", "providerId", "summary", "startTime", "endTime"));
        return schema;
    }

    private Map<String, Object> cancelEventSchema() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("calendarId", Map.of("type", "string"));
        properties.put("eventId", Map.of("type", "string"));
        return baseSchema(properties, List.of("eventId"));
    }

    private Map<String, Object> createCalendarSchema() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("summary", Map.of("type", "string"));
        properties.put("description", Map.of("type", "string"));
        return baseSchema(properties, List.of("summary"));
    }

    private Map<String, Object> scheduleAppointmentSchema() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("providerId", Map.of("type", "string"));
        properties.put("customerId", Map.of("type", "string"));
        properties.put("demandId", Map.of("type", "string"));
        properties.put("calendarId", Map.of("type", "string"));
        properties.put("summary", Map.of("type", "string"));
        properties.put("description", Map.of("type", "string"));
        properties.put("startTime", Map.of("type", "string"));
        properties.put("endTime", Map.of("type", "string"));
        properties.put("timezone", Map.of("type", "string"));
        properties.put("userIdType", Map.of("type", "string"));
        return baseSchema(properties,
            List.of("providerId", "customerId", "demandId", "summary", "startTime", "endTime"));
    }

    private Map<String, Object> baseSchema(Map<String, Object> properties, List<String> required) {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");
        schema.put("properties", properties);
        schema.put("required", required);
        return schema;
    }
}
