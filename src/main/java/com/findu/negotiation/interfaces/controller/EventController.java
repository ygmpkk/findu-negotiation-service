package com.findu.negotiation.interfaces.controller;

import com.findu.negotiation.application.CalendarBizService;
import com.findu.negotiation.domain.calendar.entity.EventEntity;
import com.findu.negotiation.domain.calendar.enums.EventStatus;
import com.findu.negotiation.domain.calendar.enums.FreeBusyStatus;
import com.findu.negotiation.domain.calendar.enums.Visibility;
import com.findu.negotiation.domain.calendar.vo.LocationVO;
import com.findu.negotiation.domain.calendar.vo.ReminderVO;
import com.findu.negotiation.infrastructure.exception.BusinessException;
import com.findu.negotiation.infrastructure.exception.ErrorCode;
import com.findu.negotiation.interfaces.dto.ApiResponse;
import com.findu.negotiation.interfaces.dto.calendar.CreateEventRequest;
import com.findu.negotiation.interfaces.dto.calendar.EventResponse;
import com.findu.negotiation.interfaces.dto.calendar.LocationRequest;
import com.findu.negotiation.interfaces.dto.calendar.ReminderRequest;
import com.findu.negotiation.interfaces.dto.calendar.UpdateEventRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 日程控制器
 * <p>
 * 提供日程的 CRUD 接口
 *
 * @author timothy
 * @date 2026/01/25
 */
@RestController
@RequestMapping("/api/v1/calendars/events")
public class EventController {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventController.class);

    @Autowired
    private CalendarBizService calendarBizService;

    /**
     * 创建日程
     *
     * @param request 创建日程请求
     * @return 日程响应
     */
    @PostMapping
    public ApiResponse<EventResponse> create(@Valid @RequestBody CreateEventRequest request) {
        LOGGER.info("创建日程请求: calendarId={}, title={}", request.getCalendarId(), request.getTitle());

        try {
            // 转换 LocationRequest 到 LocationVO
            LocationVO location = convertToLocationVO(request.getLocation());

            // 转换 ReminderRequest 到 ReminderVO
            List<ReminderVO> reminders = request.getReminders() != null
                    ? request.getReminders().stream()
                    .map(this::convertToReminderVO)
                    .collect(Collectors.toList())
                    : List.of();

            // 设置默认值
            Integer visibility = request.getVisibility() != null
                    ? request.getVisibility()
                    : Visibility.PRIVATE.getCode();
            String timezone = request.getTimezone() != null
                    ? request.getTimezone()
                    : "Asia/Shanghai";

            EventEntity event = EventEntity.builder()
                    .calendarId(request.getCalendarId())
                    .title(request.getTitle())
                    .description(request.getDescription())
                    .visibility(visibility)
                    .startTime(request.getStartTime())
                    .endTime(request.getEndTime())
                    .timezone(timezone)
                    .eventType(request.getEventType())
                    .recurrence(request.getRecurrence())
                    .status(EventStatus.SCHEDULED.getCode())
                    .freeBusyStatus(FreeBusyStatus.BUSY.getCode())
                    .reminderVOS(reminders)
                    .locationVO(location)
                    .build();

            EventEntity created = calendarBizService.createEvent(event);

            LOGGER.info("日程创建成功: eventId={}", created.getEventId());
            return ApiResponse.success(EventResponse.fromEntity(created));

        } catch (BusinessException e) {
            LOGGER.error("创建日程失败，业务错误: {}", e.getMessage());
            return ApiResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            LOGGER.error("创建日程失败，系统错误", e);
            return ApiResponse.error(ErrorCode.INTERNAL_ERROR.getCode(), "创建日程异常，请稍后重试");
        }
    }

    /**
     * 更新日程
     *
     * @param eventId 日程ID
     * @param request 更新日程请求
     * @return 日程响应
     */
    @PutMapping("/{eventId}")
    public ApiResponse<EventResponse> update(
            @PathVariable String eventId,
            @Valid @RequestBody UpdateEventRequest request) {

        LOGGER.info("更新日程请求: eventId={}", eventId);

        try {
            // 先获取现有日程
            EventEntity existing = calendarBizService.getEvent(eventId);
            if (existing == null) {
                return ApiResponse.error(ErrorCode.NOT_FOUND.getCode(), "日程不存在");
            }

            // 转换 LocationRequest 到 LocationVO
            LocationVO location = request.getLocation() != null
                    ? convertToLocationVO(request.getLocation())
                    : existing.getLocationVO();

            // 转换 ReminderRequest 到 ReminderVO
            List<ReminderVO> reminders = request.getReminders() != null
                    ? request.getReminders().stream()
                    .map(this::convertToReminderVO)
                    .collect(Collectors.toList())
                    : existing.getReminderVOS();

            EventEntity event = EventEntity.builder()
                    .eventId(eventId)
                    .calendarId(existing.getCalendarId())
                    .title(request.getTitle() != null ? request.getTitle() : existing.getTitle())
                    .description(request.getDescription() != null ? request.getDescription() : existing.getDescription())
                    .visibility(request.getVisibility() != null ? request.getVisibility() : existing.getVisibility())
                    .startTime(request.getStartTime() != null ? request.getStartTime() : existing.getStartTime())
                    .endTime(request.getEndTime() != null ? request.getEndTime() : existing.getEndTime())
                    .timezone(request.getTimezone() != null ? request.getTimezone() : existing.getTimezone())
                    .eventType(existing.getEventType())
                    .recurrence(existing.getRecurrence())
                    .status(request.getStatus() != null ? request.getStatus() : existing.getStatus())
                    .freeBusyStatus(request.getFreeBusyStatus() != null ? request.getFreeBusyStatus() : existing.getFreeBusyStatus())
                    .reminderVOS(reminders)
                    .locationVO(location)
                    .isException(existing.getIsException())
                    .recurringEventId(existing.getRecurringEventId())
                    .gmtCreate(existing.getGmtCreate())
                    .gmtModify(existing.getGmtModify())
                    .build();

            EventEntity updated = calendarBizService.updateEvent(event);

            LOGGER.info("日程更新成功: eventId={}", updated.getEventId());
            return ApiResponse.success(EventResponse.fromEntity(updated));

        } catch (BusinessException e) {
            LOGGER.error("更新日程失败，业务错误: {}", e.getMessage());
            return ApiResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            LOGGER.error("更新日程失败，系统错误", e);
            return ApiResponse.error(ErrorCode.INTERNAL_ERROR.getCode(), "更新日程异常，请稍后重试");
        }
    }

    /**
     * 删除日程
     *
     * @param eventId 日程ID
     * @return 操作结果
     */
    @DeleteMapping("/{eventId}")
    public ApiResponse<Void> delete(@PathVariable String eventId) {
        LOGGER.info("删除日程请求: eventId={}", eventId);

        try {
            calendarBizService.deleteEvent(eventId);

            LOGGER.info("日程删除成功: eventId={}", eventId);
            return ApiResponse.success(null);

        } catch (BusinessException e) {
            LOGGER.error("删除日程失败，业务错误: {}", e.getMessage());
            return ApiResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            LOGGER.error("删除日程失败，系统错误", e);
            return ApiResponse.error(ErrorCode.INTERNAL_ERROR.getCode(), "删除日程异常，请稍后重试");
        }
    }

    /**
     * 获取日程详情
     *
     * @param eventId 日程ID
     * @return 日程响应
     */
    @GetMapping("/{eventId}")
    public ApiResponse<EventResponse> get(@PathVariable String eventId) {
        LOGGER.info("获取日程请求: eventId={}", eventId);

        try {
            EventEntity event = calendarBizService.getEvent(eventId);

            if (event == null) {
                return ApiResponse.error(ErrorCode.NOT_FOUND.getCode(), "日程不存在");
            }

            return ApiResponse.success(EventResponse.fromEntity(event));

        } catch (Exception e) {
            LOGGER.error("获取日程失败，系统错误", e);
            return ApiResponse.error(ErrorCode.INTERNAL_ERROR.getCode(), "获取日程异常，请稍后重试");
        }
    }

    /**
     * 获取日历中的所有日程
     *
     * @param calendarId 日历ID
     * @return 日程列表
     */
    @GetMapping("/calendar/{calendarId}")
    public ApiResponse<List<EventResponse>> listByCalendar(@PathVariable String calendarId) {
        LOGGER.info("获取日历日程列表: calendarId={}", calendarId);

        try {
            List<EventEntity> events = calendarBizService.getEventsByCalendar(calendarId);
            List<EventResponse> responses = EventResponse.fromEntities(events);

            LOGGER.info("获取日程列表成功: count={}", responses.size());
            return ApiResponse.success(responses);

        } catch (Exception e) {
            LOGGER.error("获取日程列表失败，系统错误", e);
            return ApiResponse.error(ErrorCode.INTERNAL_ERROR.getCode(), "获取日程列表异常，请稍后重试");
        }
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 转换 LocationRequest 到 LocationVO
     */
    private LocationVO convertToLocationVO(LocationRequest request) {
        if (request == null) {
            return null;
        }
        return LocationVO.builder()
                .name(request.getName())
                .address(request.getAddress())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .build();
    }

    /**
     * 转换 ReminderRequest 到 ReminderVO
     */
    private ReminderVO convertToReminderVO(ReminderRequest request) {
        return ReminderVO.builder()
                .minutes(request.getMinutes() != null ? request.getMinutes() : 0)
                .hour(request.getHour() != null ? request.getHour() : 0)
                .day(request.getDay() != null ? request.getDay() : 0)
                .build();
    }
}
