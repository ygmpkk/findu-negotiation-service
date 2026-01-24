package com.findu.negotiation.interfaces.controller;

import com.findu.negotiation.application.CalendarBizService;
import com.findu.negotiation.interfaces.dto.ApiResponse;
import com.findu.negotiation.interfaces.dto.calendar.*;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/feishu/calendar")
public class CalendarController {

    private static final Logger LOGGER = LoggerFactory.getLogger(CalendarController.class);

    private final CalendarBizService calendarBizService;

    public CalendarController(CalendarBizService calendarBizService) {
        this.calendarBizService = calendarBizService;
    }

    @PostMapping("/freebusy")
    public ApiResponse<CalendarFreeBusyResponse> getFreeBusy(@Valid @RequestBody CalendarFreeBusyRequest request) {
        LOGGER.info("获取服务方闲忙: providerId={}, start={}, end={}",
            request.getProviderId(), request.getStartTime(), request.getEndTime());
        return ApiResponse.success(calendarBizService.getFreeBusy(request));
    }

    @PostMapping("/schedule")
    public ApiResponse<CalendarScheduleResponse> getSchedule(@Valid @RequestBody CalendarScheduleRequest request) {
        LOGGER.info("获取教练可预约日历: providerId={}, viewerRole={}",
            request.getProviderId(), request.getViewerRole());
        return ApiResponse.success(calendarBizService.getSchedule(request));
    }

    @PostMapping("/calendars")
    public ApiResponse<CalendarCreateResponse> createCalendar(@Valid @RequestBody CalendarCreateRequest request) {
        LOGGER.info("创建飞书日历: summary={}", request.getSummary());
        return ApiResponse.success(calendarBizService.createCalendar(request));
    }

    @PostMapping("/events")
    public ApiResponse<CalendarEventResponse> createEvent(@Valid @RequestBody CalendarEventCreateRequest request) {
        LOGGER.info("创建飞书日历事件: providerId={}, summary={}", request.getProviderId(), request.getSummary());
        return ApiResponse.success(calendarBizService.createEvent(request));
    }

    @PutMapping("/events/{eventId}")
    public ApiResponse<CalendarEventResponse> updateEvent(@PathVariable String eventId,
                                                          @Valid @RequestBody CalendarEventUpdateRequest request) {
        LOGGER.info("更新飞书日历事件: eventId={}, providerId={}", eventId, request.getProviderId());
        return ApiResponse.success(calendarBizService.updateEvent(eventId, request));
    }

    @DeleteMapping("/events/{eventId}")
    public ApiResponse<Void> cancelEvent(@PathVariable String eventId,
                                         @RequestParam(required = false) String calendarId) {
        LOGGER.info("取消飞书日历事件: eventId={}, calendarId={}", eventId, calendarId);
        calendarBizService.cancelEvent(calendarId, eventId);
        return ApiResponse.success(null);
    }

    @PostMapping("/appointments")
    public ApiResponse<CalendarEventResponse> scheduleAppointment(@Valid @RequestBody CalendarAppointmentRequest request) {
        LOGGER.info("安排预约日历: providerId={}, customerId={}, demandId={}",
            request.getProviderId(), request.getCustomerId(), request.getDemandId());
        return ApiResponse.success(calendarBizService.scheduleAppointment(request));
    }
}
