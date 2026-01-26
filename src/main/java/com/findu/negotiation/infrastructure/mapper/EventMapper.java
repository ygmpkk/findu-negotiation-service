package com.findu.negotiation.infrastructure.mapper;

import com.findu.negotiation.domain.calendar.entity.EventEntity;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 日程数据访问接口
 * <p>
 * 提供 Event 表的 CRUD 操作
 * 所有写操作（insert/update）返回影响的行数
 *
 * @author timothy
 * @date 2026/01/25
 */
@Mapper
public interface EventMapper {

    /**
     * 插入日程
     *
     * @param eventEntity 日程实体
     * @return 影响的行数
     */
    int insert(EventEntity eventEntity);

    /**
     * 选择性插入日程（只插入非空字段）
     *
     * @param eventEntity 日程实体
     * @return 影响的行数
     */
    int insertSelective(EventEntity eventEntity);

    /**
     * 更新日程
     *
     * @param eventEntity 日程实体
     * @return 影响的行数
     */
    int update(EventEntity eventEntity);

    /**
     * 根据 ID 查找日程
     *
     * @param eventId 日程ID
     * @return 日程实体
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
    List<EventEntity> findByCalendarIdAndStatus(String calendarId, Integer status);

    /**
     * 根据日历 ID 和日程类型查找日程列表
     *
     * @param calendarId 日历ID
     * @param eventType  日程类型
     * @return 日程列表
     */
    List<EventEntity> findByCalendarIdAndEventType(String calendarId, Integer eventType);

    /**
     * 查找指定时间范围内的日程
     *
     * @param calendarId 日历ID
     * @param startTime  开始时间
     * @param endTime    结束时间
     * @return 日程列表
     */
    List<EventEntity> findByCalendarIdAndTimeRange(String calendarId, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 查找指定时间范围内且状态为 SCHEDULED 的日程
     *
     * @param calendarId 日历ID
     * @param startTime  开始时间
     * @param endTime    结束时间
     * @return 日程列表
     */
    List<EventEntity> findScheduledByCalendarIdAndTimeRange(String calendarId, LocalDateTime startTime, LocalDateTime endTime);

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
     * 删除日程
     *
     * @param eventId 日程ID
     * @return 影响的行数
     */
    int deleteById(String eventId);

    /**
     * 检查日程是否存在
     *
     * @param eventId 日程ID
     * @return 是否存在
     */
    boolean existsById(String eventId);

    /**
     * 检查日历是否拥有指定日程
     *
     * @param eventId   日程ID
     * @param calendarId 日历ID
     * @return 是否拥有
     */
    boolean existsByEventIdAndCalendarId(String eventId, String calendarId);

    /**
     * 批量插入日程
     *
     * @param eventEntities 日程实体列表
     * @return 影响的行数
     */
    int insertBatch(List<EventEntity> eventEntities);

    /**
     * 批量删除日程
     *
     * @param eventIds 日程ID列表
     * @return 影响的行数
     */
    int deleteBatch(List<String> eventIds);

    /**
     * 删除日历下的所有日程
     *
     * @param calendarId 日历ID
     * @return 影响的行数
     */
    int deleteAllByCalendarId(String calendarId);
}
