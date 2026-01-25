package com.findu.negotiation.domain.calendar.repository;

import com.findu.negotiation.domain.calendar.entity.CalendarEntity;
import com.findu.negotiation.domain.calendar.enums.CalendarStatus;

import java.util.List;
import java.util.Optional;

/**
 * 日历仓储接口
 * <p>
 * 提供日历实体的持久化操作，封装底层 Mapper 实现细节
 *
 * @author timothy
 * @date 2026/01/25
 */
public interface CalendarRepository {

    /**
     * 保存日历
     *
     * @param calendar 日历实体
     * @return 保存后的日历
     */
    CalendarEntity save(CalendarEntity calendar);

    /**
     * 批量保存日历
     *
     * @param calendars 日历实体列表
     * @return 保存后的日历列表
     */
    List<CalendarEntity> saveAll(List<CalendarEntity> calendars);

    /**
     * 根据 ID 查找日历
     *
     * @param calendarId 日历ID
     * @return 日历实体，不存在返回空
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
     * @param status     日历状态
     * @return 日历列表
     */
    List<CalendarEntity> findByProviderIdAndStatus(String providerId, CalendarStatus status);

    /**
     * 查找所有日历
     *
     * @return 所有日历列表
     */
    List<CalendarEntity> findAll();

    /**
     * 判断日历是否存在
     *
     * @param calendarId 日历ID
     * @return 是否存在
     */
    boolean existsById(String calendarId);

    /**
     * 统计服务提供方的日历数量
     *
     * @param providerId 服务提供方ID
     * @return 日历数量
     */
    long countByProviderId(String providerId);

    /**
     * 删除日历
     *
     * @param calendarId 日历ID
     */
    void deleteById(String calendarId);

    /**
     * 批量删除日历
     *
     * @param calendarIds 日历ID列表
     */
    void deleteAllById(List<String> calendarIds);

    /**
     * 删除服务提供方的所有日历
     *
     * @param providerId 服务提供方ID
     */
    void deleteAllByProviderId(String providerId);
}
