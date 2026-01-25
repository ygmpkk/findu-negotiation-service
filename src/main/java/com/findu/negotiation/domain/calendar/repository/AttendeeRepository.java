package com.findu.negotiation.domain.calendar.repository;

import com.findu.negotiation.domain.calendar.entity.AttendeeEntity;
import com.findu.negotiation.domain.calendar.enums.AttendeeRole;
import com.findu.negotiation.domain.calendar.enums.RsvpStatus;

import java.util.List;
import java.util.Optional;

/**
 * 参与人仓储接口
 * <p>
 * 提供参与人实体的持久化操作，封装底层 Mapper 实现细节
 *
 * @author timothy
 * @date 2026/01/25
 */
public interface AttendeeRepository {

    /**
     * 保存参与人
     *
     * @param attendee 参与人实体
     * @return 保存后的参与人
     */
    AttendeeEntity save(AttendeeEntity attendee);

    /**
     * 批量保存参与人
     *
     * @param attendees 参与人实体列表
     * @return 保存后的参与人列表
     */
    List<AttendeeEntity> saveAll(List<AttendeeEntity> attendees);

    /**
     * 根据 ID 查找参与人
     *
     * @param attendeeId 参与人ID
     * @return 参与人实体，不存在返回空
     */
    Optional<AttendeeEntity> findById(String attendeeId);

    /**
     * 根据日程 ID 查找参与人列表
     *
     * @param eventId 日程ID
     * @return 参与人列表
     */
    List<AttendeeEntity> findByEventId(String eventId);

    /**
     * 根据客户 ID 查找参与人列表
     *
     * @param customerId 客户ID
     * @return 参与人列表
     */
    List<AttendeeEntity> findByCustomerId(String customerId);

    /**
     * 根据日程 ID 和客户 ID 查找参与人
     *
     * @param eventId    日程ID
     * @param customerId 客户ID
     * @return 参与人实体，不存在返回空
     */
    Optional<AttendeeEntity> findByEventIdAndCustomerId(String eventId, String customerId);

    /**
     * 根据日程 ID 和 RSVP 状态查找参与人列表
     *
     * @param eventId 日程ID
     * @param status  RSVP 状态
     * @return 参与人列表
     */
    List<AttendeeEntity> findByEventIdAndRsvpStatus(String eventId, RsvpStatus status);

    /**
     * 根据日程 ID 和角色查找参与人列表
     *
     * @param eventId 日程ID
     * @param role    参与角色
     * @return 参与人列表
     */
    List<AttendeeEntity> findByEventIdAndRole(String eventId, AttendeeRole role);

    /**
     * 判断参与人是否存在
     *
     * @param attendeeId 参与人ID
     * @return 是否存在
     */
    boolean existsById(String attendeeId);

    /**
     * 判断日程是否包含指定参与人
     *
     * @param eventId    日程ID
     * @param customerId 客户ID
     * @return 是否包含
     */
    boolean existsByEventIdAndCustomerId(String eventId, String customerId);

    /**
     * 统计日程的参与人数量
     *
     * @param eventId 日程ID
     * @return 参与人数量
     */
    long countByEventId(String eventId);

    /**
     * 统计日程中已接受的参与人数量
     *
     * @param eventId 日程ID
     * @return 已接受的参与人数量
     */
    long countAcceptedByEventId(String eventId);

    /**
     * 删除参与人
     *
     * @param attendeeId 参与人ID
     */
    void deleteById(String attendeeId);

    /**
     * 批量删除参与人
     *
     * @param attendeeIds 参与人ID列表
     */
    void deleteAllById(List<String> attendeeIds);

    /**
     * 删除日程的所有参与人
     *
     * @param eventId 日程ID
     */
    void deleteAllByEventId(String eventId);
}
