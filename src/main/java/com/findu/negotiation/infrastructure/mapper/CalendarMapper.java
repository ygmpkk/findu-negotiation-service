package com.findu.negotiation.infrastructure.mapper;

import com.findu.negotiation.domain.calendar.entity.CalendarEntity;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Optional;

/**
 * 日历数据访问接口
 * <p>
 * 提供 Calendar 表的 CRUD 操作
 * 所有写操作（insert/update）返回影响的行数
 *
 * @author timothy
 * @date 2026/01/25
 */
@Mapper
public interface CalendarMapper {

    /**
     * 插入日历
     *
     * @param calendarEntity 日历实体
     * @return 影响的行数
     */
    int insert(CalendarEntity calendarEntity);

    /**
     * 选择性插入日历（只插入非空字段）
     *
     * @param calendarEntity 日历实体
     * @return 影响的行数
     */
    int insertSelective(CalendarEntity calendarEntity);

    /**
     * 更新日历
     *
     * @param calendarEntity 日历实体
     * @return 影响的行数
     */
    int update(CalendarEntity calendarEntity);

    /**
     * 根据 ID 查找日历
     *
     * @param calendarId 日历ID
     * @return 日历实体
     */
    Optional<CalendarEntity> findById(String calendarId);

    /**
     * 根据服务提供方 ID 查找日历列表
     *
     * @param providerId 服务提供方ID
     * @return 日历列表
     */
    List<CalendarEntity> findByProviderId(String providerId);

    /**
     * 根据服务提供方 ID 和状态查找日历列表
     *
     * @param providerId 服务提供方ID
     * @param status     日程状态
     * @return 日程列表
     */
    List<CalendarEntity> findByProviderIdAndStatus(String providerId, Integer status);

    /**
     * 删除日历
     *
     * @param calendarId 日历ID
     * @return 影响的行数
     */
    int deleteById(String calendarId);

    /**
     * 检查日历是否存在
     *
     * @param calendarId 日历ID
     * @return 是否存在
     */
    boolean existsById(String calendarId);

    /**
     * 检查服务提供方是否拥有指定日历
     *
     * @param calendarId 日历ID
     * @param providerId 服务提供方ID
     * @return 是否拥有
     */
    boolean existsByCalendarIdAndProviderId(String calendarId, String providerId);

    /**
     * 批量插入日历
     *
     * @param calendarEntities 日历实体列表
     * @return 影响的行数
     */
    int insertBatch(List<CalendarEntity> calendarEntities);
}
