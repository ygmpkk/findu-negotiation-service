package com.findu.negotiation.infrastructure.mapper;

import com.findu.negotiation.domain.calendar.entity.AttendeeEntity;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Optional;

/**
 * 日程参与人数据访问接口
 * <p>
 * 提供 Attendee 表的 CRUD 操作
 * 所有写操作（insert/update）返回影响的行数
 *
 * @author timothy
 * @date 2026/01/25
 */
@Mapper
public interface AttendeeMapper {

    /**
     * 插入参与人
     *
     * @param attendeeEntity 参与人实体
     * @return 影响的行数
     */
    int insert(AttendeeEntity attendeeEntity);

    /**
     * 更新参与人
     *
     * @param attendeeEntity 参与人实体
     * @return 影响的行数
     */
    int update(AttendeeEntity attendeeEntity);

    /**
     * 根据 ID 查找参与人
     *
     * @param attendeeId 参与人ID
     * @return 参与人实体
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
     * 根据日程 ID 和 客户 ID 查找参与人
     *
     * @param eventId    日程ID
     * @param customerId 客户ID
     * @return 参与人实体
     */
    Optional<AttendeeEntity> findByEventIdAndCustomerId(String eventId, String customerId);

    /**
     * 根据日程 ID 和 RSVP 状态查找参与人列表
     *
     * @param eventId 日程ID
     * @param status  RSVP 状态
     * @return 参与人列表
     */
    List<AttendeeEntity> findByEventIdAndRsvpStatus(String eventId, Integer status);

    /**
     * 根据日程 ID 和角色查找参与人列表
     *
     * @param eventId 日程ID
     * @param role    参与角色
     * @return 参与人列表
     */
    List<AttendeeEntity> findByEventIdAndRole(String eventId, Integer role);

    /**
     * 删除参与人
     *
     * @param attendeeId 参与人ID
     * @return 影响的行数
     */
    int deleteById(String attendeeId);

    /**
     * 删除日程的所有参与人
     *
     * @param eventId 日程ID
     * @return 影响的行数
     */
    int deleteAllByEventId(String eventId);

    /**
     * 检查参与人是否存在
     *
     * @param attendeeId 参与人ID
     * @return 是否存在
     */
    boolean existsById(String attendeeId);

    /**
     * 检查日程是否包含指定参与人
     *
     * @param eventId    日程ID
     * @param customerId 客户ID
     * @return 是否包含
     */
    boolean existsByEventIdAndCustomerId(String eventId, String customerId);

    /**
     * 批量插入参与人
     *
     * @param attendeeEntities 参与人实体列表
     * @return 影响的行数
     */
    int insertBatch(List<AttendeeEntity> attendeeEntities);

    /**
     * 批量删除参与人
     *
     * @param attendeeIds 参与人ID列表
     * @return 影响的行数
     */
    int deleteBatch(List<String> attendeeIds);

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
}
