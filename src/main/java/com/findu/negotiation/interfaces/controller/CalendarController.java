package com.findu.negotiation.interfaces.controller;

import com.findu.negotiation.application.CalendarBizService;
import com.findu.negotiation.application.CalendarBizService.FreeBusy;
import com.findu.negotiation.domain.calendar.entity.CalendarEntity;
import com.findu.negotiation.infrastructure.exception.BusinessException;
import com.findu.negotiation.infrastructure.exception.ErrorCode;
import com.findu.negotiation.interfaces.dto.ApiResponse;
import com.findu.negotiation.interfaces.dto.calendar.CalendarResponse;
import com.findu.negotiation.interfaces.dto.calendar.CreateCalendarRequest;
import com.findu.negotiation.interfaces.dto.calendar.FreeBusyResponse;
import com.findu.negotiation.interfaces.dto.calendar.UpdateCalendarRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 日历控制器
 * <p>
 * 提供日历的 CRUD 接口
 *
 * @author timothy
 * @date 2026/01/25
 */
@RestController
@RequestMapping("/api/v1/calendars")
public class CalendarController {

    private static final Logger LOGGER = LoggerFactory.getLogger(CalendarController.class);

    @Autowired
    private CalendarBizService calendarBizService;

    /**
     * 创建日历
     *
     * @param request 创建日历请求
     * @return 日历响应
     */
    @PostMapping
    public ApiResponse<CalendarResponse> create(@Valid @RequestBody CreateCalendarRequest request) {
        LOGGER.info("创建日历请求: providerId={}, name={}", request.getProviderId(), request.getName());

        try {
            CalendarEntity calendar = CalendarEntity.builder()
                    .providerId(request.getProviderId())
                    .name(request.getName())
                    .description(request.getDescription())
                    .timezone(request.getTimezone() != null ? request.getTimezone() : "Asia/Shanghai")
                    .status(com.findu.negotiation.domain.calendar.enums.CalendarStatus.ACTIVE.getCode())
                    .build();

            CalendarEntity created = calendarBizService.createCalendar(calendar);

            LOGGER.info("日历创建成功: calendarId={}", created.getCalendarId());
            return ApiResponse.success(CalendarResponse.fromEntity(created));

        } catch (BusinessException e) {
            LOGGER.error("创建日历失败，业务错误: {}", e.getMessage());
            return ApiResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            LOGGER.error("创建日历失败，系统错误", e);
            return ApiResponse.error(ErrorCode.INTERNAL_ERROR.getCode(), "创建日历异常，请稍后重试");
        }
    }

    /**
     * 更新日历
     *
     * @param calendarId 日历ID
     * @param request    更新日历请求
     * @return 日历响应
     */
    @PutMapping("/{calendarId}")
    public ApiResponse<CalendarResponse> update(
            @PathVariable String calendarId,
            @Valid @RequestBody UpdateCalendarRequest request) {

        LOGGER.info("更新日历请求: calendarId={}", calendarId);

        try {
            CalendarEntity calendar = CalendarEntity.builder()
                    .calendarId(calendarId)
                    .name(request.getName())
                    .description(request.getDescription())
                    .timezone(request.getTimezone())
                    .status(request.getStatus())
                    .build();

            CalendarEntity updated = calendarBizService.updateCalendar(calendar);

            LOGGER.info("日历更新成功: calendarId={}", updated.getCalendarId());
            return ApiResponse.success(CalendarResponse.fromEntity(updated));

        } catch (BusinessException e) {
            LOGGER.error("更新日历失败，业务错误: {}", e.getMessage());
            return ApiResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            LOGGER.error("更新日历失败，系统错误", e);
            return ApiResponse.error(ErrorCode.INTERNAL_ERROR.getCode(), "更新日历异常，请稍后重试");
        }
    }

    /**
     * 删除日历
     *
     * @param calendarId 日历ID
     * @return 操作结果
     */
    @DeleteMapping("/{calendarId}")
    public ApiResponse<Void> delete(@PathVariable String calendarId) {
        LOGGER.info("删除日历请求: calendarId={}", calendarId);

        try {
            calendarBizService.deleteCalendar(calendarId);

            LOGGER.info("日历删除成功: calendarId={}", calendarId);
            return ApiResponse.success(null);

        } catch (BusinessException e) {
            LOGGER.error("删除日历失败，业务错误: {}", e.getMessage());
            return ApiResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            LOGGER.error("删除日历失败，系统错误", e);
            return ApiResponse.error(ErrorCode.INTERNAL_ERROR.getCode(), "删除日历异常，请稍后重试");
        }
    }

    /**
     * 获取日历详情
     *
     * @param calendarId 日历ID
     * @return 日历响应
     */
    @GetMapping("/{calendarId}")
    public ApiResponse<CalendarResponse> get(@PathVariable String calendarId) {
        LOGGER.info("获取日历请求: calendarId={}", calendarId);

        try {
            CalendarEntity calendar = calendarBizService.getCalendar(calendarId);

            if (calendar == null) {
                return ApiResponse.error(ErrorCode.NOT_FOUND.getCode(), "日历不存在");
            }

            return ApiResponse.success(CalendarResponse.fromEntity(calendar));

        } catch (Exception e) {
            LOGGER.error("获取日历失败，系统错误", e);
            return ApiResponse.error(ErrorCode.INTERNAL_ERROR.getCode(), "获取日历异常，请稍后重试");
        }
    }

    /**
     * 获取服务提供方的日历列表
     *
     * @param providerId 服务提供方ID
     * @return 日历列表
     */
    @GetMapping("/provider/{providerId}")
    public ApiResponse<List<CalendarResponse>> listByProvider(@PathVariable String providerId) {
        LOGGER.info("获取服务提供方日历列表: providerId={}", providerId);

        try {
            List<CalendarEntity> calendars = calendarBizService.getCalendarsByProvider(providerId);
            List<CalendarResponse> responses = CalendarResponse.fromEntities(calendars);

            LOGGER.info("获取日历列表成功: count={}", responses.size());
            return ApiResponse.success(responses);

        } catch (Exception e) {
            LOGGER.error("获取日历列表失败，系统错误", e);
            return ApiResponse.error(ErrorCode.INTERNAL_ERROR.getCode(), "获取日历列表异常，请稍后重试");
        }
    }

    /**
     * 获取服务提供方的激活日历列表
     *
     * @param providerId 服务提供方ID
     * @return 激活的日历列表
     */
    @GetMapping("/provider/{providerId}/active")
    public ApiResponse<List<CalendarResponse>> listActiveByProvider(@PathVariable String providerId) {
        LOGGER.info("获取服务提供方激活日历列表: providerId={}", providerId);

        try {
            List<CalendarEntity> calendars = calendarBizService.getActiveCalendars(providerId);
            List<CalendarResponse> responses = CalendarResponse.fromEntities(calendars);

            LOGGER.info("获取激活日历列表成功: count={}", responses.size());
            return ApiResponse.success(responses);

        } catch (Exception e) {
            LOGGER.error("获取激活日历列表失败，系统错误", e);
            return ApiResponse.error(ErrorCode.INTERNAL_ERROR.getCode(), "获取激活日历列表异常，请稍后重试");
        }
    }

    /**
     * 获取闲忙日历
     * <p>
     * 返回服务提供方在指定时间范围内的忙碌时间段（不显示具体日程详情）
     *
     * @param providerId 服务提供方ID
     * @param startTime  开始时间
     * @param endTime    结束时间
     * @param timezone   时区
     * @return 闲忙状态列表
     */
    @GetMapping("/provider/{providerId}/freebusy")
    public ApiResponse<List<FreeBusyResponse>> getFreeBusy(
            @PathVariable String providerId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime startTime,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime endTime,
            @RequestParam(defaultValue = "Asia/Shanghai") String timezone) {

        LOGGER.info("获取闲忙日历: providerId={}, startTime={}, endTime={}, timezone={}",
                providerId, startTime, endTime, timezone);

        try {
            List<FreeBusy> freeBusyList = calendarBizService.getFreeBusyStatus(
                    providerId, startTime, endTime, timezone);

            // 转换为响应 DTO
            List<FreeBusyResponse> responses = freeBusyList.stream()
                    .map(this::toFreeBusyResponse)
                    .collect(Collectors.toList());

            LOGGER.info("获取闲忙日历成功: calendars={}", responses.size());
            return ApiResponse.success(responses);

        } catch (BusinessException e) {
            LOGGER.error("获取闲忙日历失败，业务错误: {}", e.getMessage());
            return ApiResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            LOGGER.error("获取闲忙日历失败，系统错误", e);
            return ApiResponse.error(ErrorCode.INTERNAL_ERROR.getCode(), "获取闲忙日历异常，请稍后重试");
        }
    }

    /**
     * 转换为闲忙响应 DTO
     */
    private FreeBusyResponse toFreeBusyResponse(FreeBusy status) {
        List<FreeBusyResponse.BusyTimeSlot> busySlots = status.busyTimes().stream()
                .map(slot -> FreeBusyResponse.BusyTimeSlot.builder()
                        .startTime(slot.startTime())
                        .endTime(slot.endTime())
                        .status(slot.status())
                        .build())
                .collect(Collectors.toList());

        return FreeBusyResponse.builder()
                .calendarId(status.calendarId())
                .providerId(status.providerId())
                .timezone(status.timezone())
                .startTime(status.startTime())
                .endTime(status.endTime())
                .busyTimes(busySlots)
                .build();
    }
}
