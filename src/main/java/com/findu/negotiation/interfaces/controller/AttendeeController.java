package com.findu.negotiation.interfaces.controller;

import com.findu.negotiation.application.CalendarBizService;
import com.findu.negotiation.domain.calendar.entity.AttendeeEntity;
import com.findu.negotiation.domain.calendar.enums.AttendeeRole;
import com.findu.negotiation.domain.calendar.enums.RsvpStatus;
import com.findu.negotiation.infrastructure.exception.BusinessException;
import com.findu.negotiation.infrastructure.exception.ErrorCode;
import com.findu.negotiation.interfaces.dto.ApiResponse;
import com.findu.negotiation.interfaces.dto.calendar.AttendeeResponse;
import com.findu.negotiation.interfaces.dto.calendar.CreateAttendeeRequest;
import com.findu.negotiation.interfaces.dto.calendar.UpdateAttendeeRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 参与人控制器
 * <p>
 * 提供日程参与人的 CRUD 接口
 *
 * @author timothy
 * @date 2026/01/25
 */
@RestController
@RequestMapping("/api/v1/calendars/attendees")
public class AttendeeController {

    private static final Logger LOGGER = LoggerFactory.getLogger(AttendeeController.class);

    @Autowired
    private CalendarBizService calendarBizService;

    /**
     * 添加参与人
     *
     * @param request 创建参与人请求
     * @return 参与人响应
     */
    @PostMapping
    public ApiResponse<AttendeeResponse> create(@Valid @RequestBody CreateAttendeeRequest request) {
        LOGGER.info("添加参与人请求: eventId={}, customerId={}", request.getEventId(), request.getCustomerId());

        try {
            // 设置默认值
            Integer role = request.getRole() != null
                    ? request.getRole()
                    : AttendeeRole.REQUIRED.getCode();
            Integer rsvpStatus = request.getRsvpStatus() != null
                    ? request.getRsvpStatus()
                    : RsvpStatus.PENDING.getCode();

            AttendeeEntity attendee = AttendeeEntity.builder()
                    .eventId(request.getEventId())
                    .customerId(request.getCustomerId())
                    .role(role)
                    .rsvpStatus(rsvpStatus)
                    .build();

            AttendeeEntity created = calendarBizService.addAttendee(request.getEventId(), attendee);

            LOGGER.info("参与人添加成功: attendeeId={}", created.getAttendeeId());
            return ApiResponse.success(AttendeeResponse.fromEntity(created));

        } catch (BusinessException e) {
            LOGGER.error("添加参与人失败，业务错误: {}", e.getMessage());
            return ApiResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            LOGGER.error("添加参与人失败，系统错误", e);
            return ApiResponse.error(ErrorCode.INTERNAL_ERROR.getCode(), "添加参与人异常，请稍后重试");
        }
    }

    /**
     * 更新参与人
     *
     * @param attendeeId 参与人ID
     * @param request    更新参与人请求
     * @return 参与人响应
     */
    @PutMapping("/{attendeeId}")
    public ApiResponse<AttendeeResponse> update(
            @PathVariable String attendeeId,
            @Valid @RequestBody UpdateAttendeeRequest request) {

        LOGGER.info("更新参与人请求: attendeeId={}", attendeeId);

        try {
            // 更新 RSVP 状态
            if (request.getRsvpStatus() != null) {
                calendarBizService.updateRsvpStatus(attendeeId, request.getRsvpStatus());
            }

            // 注意：由于 CalendarDomainService 只提供了 updateRsvpStatus 方法
            // 如果需要更新 role，需要扩展 domain service
            // 这里暂时只返回成功响应
            LOGGER.info("参与人更新成功: attendeeId={}", attendeeId);
            return ApiResponse.success(null);

        } catch (BusinessException e) {
            LOGGER.error("更新参与人失败，业务错误: {}", e.getMessage());
            return ApiResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            LOGGER.error("更新参与人失败，系统错误", e);
            return ApiResponse.error(ErrorCode.INTERNAL_ERROR.getCode(), "更新参与人异常，请稍后重试");
        }
    }

    /**
     * 移除参与人
     *
     * @param attendeeId 参与人ID
     * @param eventId    日程ID
     * @return 操作结果
     */
    @DeleteMapping("/{attendeeId}")
    public ApiResponse<Void> delete(
            @PathVariable String attendeeId,
            @RequestParam String eventId) {

        LOGGER.info("移除参与人请求: attendeeId={}, eventId={}", attendeeId, eventId);

        try {
            calendarBizService.removeAttendee(eventId, attendeeId);

            LOGGER.info("参与人移除成功: attendeeId={}", attendeeId);
            return ApiResponse.success(null);

        } catch (BusinessException e) {
            LOGGER.error("移除参与人失败，业务错误: {}", e.getMessage());
            return ApiResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            LOGGER.error("移除参与人失败，系统错误", e);
            return ApiResponse.error(ErrorCode.INTERNAL_ERROR.getCode(), "移除参与人异常，请稍后重试");
        }
    }

    /**
     * 获取参与人详情
     *
     * @param attendeeId 参与人ID
     * @return 参与人响应
     */
    @GetMapping("/{attendeeId}")
    public ApiResponse<AttendeeResponse> get(@PathVariable String attendeeId) {
        LOGGER.info("获取参与人请求: attendeeId={}", attendeeId);

        try {
            AttendeeEntity attendee = calendarBizService.getAttendee(attendeeId);

            if (attendee == null) {
                return ApiResponse.error(ErrorCode.NOT_FOUND.getCode(), "参与人不存在");
            }

            return ApiResponse.success(AttendeeResponse.fromEntity(attendee));

        } catch (Exception e) {
            LOGGER.error("获取参与人失败，系统错误", e);
            return ApiResponse.error(ErrorCode.INTERNAL_ERROR.getCode(), "获取参与人异常，请稍后重试");
        }
    }

    /**
     * 获取日程的所有参与人
     *
     * @param eventId 日程ID
     * @return 参与人列表
     */
    @GetMapping("/event/{eventId}")
    public ApiResponse<List<AttendeeResponse>> listByEvent(@PathVariable String eventId) {
        LOGGER.info("获取日程参与人列表: eventId={}", eventId);

        try {
            List<AttendeeEntity> attendees = calendarBizService.getAttendees(eventId);
            List<AttendeeResponse> responses = AttendeeResponse.fromEntities(attendees);

            LOGGER.info("获取参与人列表成功: count={}", responses.size());
            return ApiResponse.success(responses);

        } catch (Exception e) {
            LOGGER.error("获取参与人列表失败，系统错误", e);
            return ApiResponse.error(ErrorCode.INTERNAL_ERROR.getCode(), "获取参与人列表异常，请稍后重试");
        }
    }

    /**
     * 更新参与人 RSVP 状态
     *
     * @param attendeeId 参与人ID
     * @param status     RSVP 状态 (0: 待响应, 1: 已接受, 2: 已拒绝)
     * @return 操作结果
     */
    @PatchMapping("/{attendeeId}/rsvp")
    public ApiResponse<Void> updateRsvp(
            @PathVariable String attendeeId,
            @RequestParam Integer status) {

        LOGGER.info("更新参与人RSVP状态: attendeeId={}, status={}", attendeeId, status);

        try {
            calendarBizService.updateRsvpStatus(attendeeId, status);

            LOGGER.info("RSVP状态更新成功: attendeeId={}", attendeeId);
            return ApiResponse.success(null);

        } catch (BusinessException e) {
            LOGGER.error("更新RSVP状态失败，业务错误: {}", e.getMessage());
            return ApiResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            LOGGER.error("更新RSVP状态失败，系统错误", e);
            return ApiResponse.error(ErrorCode.INTERNAL_ERROR.getCode(), "更新RSVP状态异常，请稍后重试");
        }
    }
}
