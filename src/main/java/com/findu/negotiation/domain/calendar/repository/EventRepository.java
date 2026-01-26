package com.findu.negotiation.domain.calendar.repository;

import com.findu.negotiation.domain.calendar.entity.EventEntity;
import com.findu.negotiation.domain.calendar.enums.EventStatus;
import com.findu.negotiation.domain.calendar.enums.EventType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 日程仓储接口
 * <p>
 * 提供日程实体的持久化操作，封装底层 Mapper 实现细节
 *
 * @author timothy
 * @date 2026/01/25
 */
public interface EventRepository {

    /**
     * 保存日程
     *
     * @param event 日程实体
     * @return 保存后的日程
     */
    EventEntity save(EventEntity event);

    /**
     * 批量保存日程
     *
     * @param events 日程实体列表
     * @return 保存后的日程列表
     */
    List<EventEntity> saveAll(List<EventEntity> events);

    /**
     * 根据 ID 查找日程
     *
     * @param eventId 日程ID
     * @return 日程实体，不存在返回空
     */
    Optional<EventEntity> findById(String eventId);

    /**
     * 根据日历 ID 查找日程列表
     *
     * @param calendarId 日历ID
     * @return 日程列表
     */
    List<EventEntity> findByCalendarId(String calendarId);

    /**
     * 根据日历 ID 和状态查找日程列表
     *
     * @param calendarId 日历ID
     * @param status     日程状态
     * @return 日程列表
     */
    List<EventEntity> findByCalendarIdAndStatus(String calendarId, EventStatus status);

    /**
     * 根据日历 ID 和日程类型查找日程列表
     *
     * @param calendarId 日历ID
     * @param eventType  日程类型
     * @return 日程列表
     */
    List<EventEntity> findByCalendarIdAndEventType(String calendarId, EventType eventType);

    /**
     * 查找指定时间范围内的日程
     *
     * @param calendarId 日历ID
     * @param startTime  开始时间
     * @param endTime    结束时间
     * @param timezone   时区
     * @return 日程列表
     */
    List<EventEntity> findByTimeRange(String calendarId, LocalDateTime startTime, LocalDateTime endTime, String timezone);

    /**
     * 查找指定时间范围内且状态为 SCHEDULED 的日程
     *
     * @param calendarId 日历ID
     * @param startTime  开始时间
     * @param endTime    结束时间
     * @param timezone   时区
     * @return 日程列表
     */
    List<EventEntity> findScheduledByTimeRange(String calendarId, LocalDateTime startTime, LocalDateTime endTime, String timezone);

    /**
     * 查找重复日程
     *
     * @param calendarId 日历ID
     * @return 重复日程列表
     */
    List<EventEntity> findRecurringEvents(String calendarId);

    /**
     * 根据重复日程 ID 查找例外日程
     *
     * @param recurringEventId 重复日程ID
     * @return 例外日程列表
     */
    List<EventEntity> findExceptionsByRecurringEventId(String recurringEventId);

    /**
     * 判断日程是否存在
     *
     * @param eventId 日程ID
     * @return 是否存在
     */
    boolean existsById(String eventId);

    /**
     * 统计日历的日程数量
     *
     * @param calendarId 日历ID
     * @return 日程数量
     */
    long countByCalendarId(String calendarId);

    /**
     * 删除日程
     *
     * @param eventId 日程ID
     */
    void deleteById(String eventId);

    /**
     * 批量删除日程
     *
     * @param eventIds 日程ID列表
     */
    void deleteAllById(List<String> eventIds);

    /**
     * 删除日历下的所有日程
     *
     * @param calendarId 日历ID
     */
    void deleteAllByCalendarId(String calendarId);
}
