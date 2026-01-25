-- =====================================================
-- Calendar Domain Database Schema
-- =====================================================
-- Database: MySQL
-- Author: timothy
-- Date: 2026/01/25
-- =====================================================

-- =====================================================
-- 1. Calendar Table (日历表)
-- =====================================================
-- Calendar 是包含一系列相关日程的容器，是同一类日程的实体。
-- 作为聚合根，负责管理其包含的所有 Event。
-- =====================================================

CREATE TABLE IF NOT EXISTS `calendar` (
    -- 主键
    `id`               BIGINT unsigned       NOT NULL AUTO_INCREMENT COMMENT '主键ID',

    -- 业务唯一标识
    `calendar_id`      VARCHAR(64)  NOT NULL DEFAULT '' COMMENT '日历唯一标识',

    -- 所有者信息
    `provider_id`      VARCHAR(64)  NOT NULL DEFAULT '' COMMENT '服务提供方ID',

    -- 基本信息
    `name`             VARCHAR(128) NOT NULL DEFAULT '' COMMENT '日历名称',
    `description`      VARCHAR(512) NOT NULL DEFAULT '' COMMENT '日历描述',

    -- 时区配置
    `timezone`         VARCHAR(64)  NOT NULL DEFAULT 'Asia/Shanghai' COMMENT '默认时区 (IANA Timezone，如 Asia/Shanghai, UTC)',

    -- 状态
    `status`           INT          NOT NULL DEFAULT 0 COMMENT '日历状态 (0: 禁用，1: 激活)',

    -- 审计字段
    `gmt_create`       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `gmt_modify`       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    -- 索引
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_calendar_id` (`calendar_id`),
    KEY `idx_provider_id` (`provider_id`),
    KEY `idx_status` (`status`),
    KEY `idx_provider_status` (`provider_id`, `status`),
    KEY `idx_gmt_create` (`gmt_create`)

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='日历表';


-- =====================================================
-- 2. Event Table (日程表)
-- =====================================================
-- Event 是会议预定与预约系统中的基本实体，表示一个确定的日期或时间范围。
-- Event 表示时间占用的最小业务单元。
-- =====================================================

CREATE TABLE IF NOT EXISTS `event` (
    -- 主键
    `id`               BIGINT unsigned      NOT NULL AUTO_INCREMENT COMMENT '主键ID',

    -- 业务唯一标识
    `event_id`         VARCHAR(64)  NOT NULL DEFAULT '' COMMENT '日程唯一标识',

    -- 所属日历
    `calendar_id`      VARCHAR(64)  NOT NULL DEFAULT '' COMMENT '所属日历ID',

    -- 基本信息
    `title`            VARCHAR(128) NOT NULL DEFAULT '' COMMENT '日程标题',
    `description`       VARCHAR(512) NOT NULL DEFAULT '' COMMENT '日程描述',

    -- 可见性
    `visibility`       INT          NOT NULL DEFAULT 1 COMMENT '日程可见性 (0: 公开, 1: 私有)',

    -- 时间信息
    `start_time`       DATETIME     NOT NULL COMMENT '开始时间',
    `end_time`         DATETIME     NOT NULL COMMENT '结束时间',
    `timezone`         VARCHAR(64)  NOT NULL DEFAULT 'Asia/Shanghai' COMMENT '时区 (IANA Timezone，如 Asia/Shanghai, UTC)',

    -- 日程类型
    `event_type`       INT          NOT NULL DEFAULT 0 COMMENT '日程类型 (0: 普通日程, 1: 重复日程)',

    -- 重复规则
    `recurrence`       VARCHAR(512) NOT NULL DEFAULT '' COMMENT '重复规则 (RFC5545 RRULE，如 FREQ=DAILY;COUNT=10)',

    -- 状态
    `status`           INT          NOT NULL DEFAULT 0 COMMENT '日程状态 (0: 已安排, 1: 已完成, 2: 已取消)',

    -- 闲忙状态
    `free_busy_status` INT          NOT NULL DEFAULT 0 COMMENT '闲忙状态 (0: 忙碌, 1: 空闲)',

    -- 例外日程标识
    `is_exception`     BOOLEAN      NOT NULL DEFAULT FALSE COMMENT '是否为重复日程的例外实例',
    `recurring_event_id` VARCHAR(64) NOT NULL DEFAULT '' COMMENT '如果是例外日程，指向其所属的重复日程的 event_id',

    -- 提醒设置 (JSON 数组格式存储 Reminder 结构)
    `reminders`        JSON         NOT NULL COMMENT '提前提醒设置列表 (Reminder JSON 数组: [{minutes: 15}, {minutes: 30}])',

    -- 位置信息 (JSON 格式存储 Location 结构)
    `location`         JSON         NOT NULL COMMENT '位置信息 (Location JSON: {name, address, latitude, longitude})',

    -- 审计字段
    `gmt_create`       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `gmt_modify`       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    -- 索引
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_event_id` (`event_id`),
    KEY `idx_calendar_id` (`calendar_id`),
    KEY `idx_start_time` (`start_time`),
    KEY `idx_end_time` (`end_time`),
    KEY `idx_event_type` (`event_type`),
    KEY `idx_status` (`status`),
    KEY `idx_recurring_event_id` (`recurring_event_id`),
    KEY `idx_calendar_status` (`calendar_id`, `status`),
    KEY `idx_calendar_type` (`calendar_id`, `event_type`),
    KEY `idx_calendar_time` (`calendar_id`, `start_time`, `end_time`),
    KEY `idx_gmt_create` (`gmt_create`)

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='日程表';

-- 注意：根据阿里巴巴 Java 开发手册规范，不使用数据库外键约束
-- 关联关系由应用层维护，删除操作需在业务层处理级联逻辑


-- =====================================================
-- 3. Attendee Table (日程参与人表)
-- =====================================================
-- Event 的参与客户实体（Customer）。
-- 管理客户与日程的参与关系。
-- =====================================================

CREATE TABLE IF NOT EXISTS `attendee` (
    -- 主键
    `id`               BIGINT unsigned      NOT NULL AUTO_INCREMENT COMMENT '主键ID',

    -- 业务唯一标识
    `attendee_id`      VARCHAR(64)  NOT NULL DEFAULT '' COMMENT '参与人唯一标识',

    -- 所属日程
    `event_id`         VARCHAR(64)  NOT NULL DEFAULT '' COMMENT '所属日程ID',

    -- 参与人信息
    `customer_id`      VARCHAR(64)  NOT NULL DEFAULT '' COMMENT '参与人客户ID',

    -- 参与角色
    `role`             INT          NOT NULL DEFAULT 0 COMMENT '参与角色 (0: 必需, 1: 可选)',

    -- RSVP 状态
    `rsvp_status`      INT          NOT NULL DEFAULT 0 COMMENT 'RSVP 响应状态 (0: 待响应, 1: 已接受, 2: 已拒绝)',

    -- 审计字段
    `gmt_create`       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',

    -- 索引
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_attendee_id` (`attendee_id`),
    KEY `idx_event_id` (`event_id`),
    KEY `idx_customer_id` (`customer_id`),
    KEY `idx_rsvp_status` (`rsvp_status`),
    KEY `idx_event_customer` (`event_id`, `customer_id`),
    KEY `idx_event_role` (`event_id`, `role`),
    UNIQUE KEY `uk_event_customer` (`event_id`, `customer_id`)

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='日程参与人表';

-- 注意：根据阿里巴巴 Java 开发手册规范，不使用数据库外键约束
-- 关联关系由应用层维护，删除操作需在业务层处理级联逻辑
