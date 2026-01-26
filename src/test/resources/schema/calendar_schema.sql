-- =====================================================
-- Calendar Domain Database Schema for H2 Test Database
-- =====================================================
-- Database: H2 (MySQL Mode)
-- Author: timothy
-- Date: 2026/01/25
-- =====================================================

-- =====================================================
-- 1. Calendar Table (日历表)
-- =====================================================

CREATE TABLE IF NOT EXISTS calendar (
    id               BIGINT        NOT NULL AUTO_INCREMENT PRIMARY KEY,
    calendar_id      VARCHAR(64)   NOT NULL DEFAULT '',
    provider_id      VARCHAR(64)   NOT NULL DEFAULT '',
    name             VARCHAR(128)  NOT NULL DEFAULT '',
    description      VARCHAR(512) NOT NULL DEFAULT '',
    timezone         VARCHAR(64)   DEFAULT 'Asia/Shanghai',
    status           INT           NOT NULL DEFAULT 0,
    gmt_create       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    gmt_modify       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_calendar_id ON calendar(calendar_id);
CREATE INDEX IF NOT EXISTS idx_provider_id ON calendar(provider_id);
CREATE INDEX IF NOT EXISTS idx_status ON calendar(status);
CREATE INDEX IF NOT EXISTS idx_provider_status ON calendar(provider_id, status);


-- =====================================================
-- 2. Event Table (日程表)
-- =====================================================

CREATE TABLE IF NOT EXISTS event (
    id               BIGINT        NOT NULL AUTO_INCREMENT PRIMARY KEY,
    event_id         VARCHAR(64)   NOT NULL DEFAULT '',
    calendar_id      VARCHAR(64)   NOT NULL DEFAULT '',
    title            VARCHAR(256)  NOT NULL DEFAULT '',
    description      VARCHAR(1024),
    visibility       INT           NOT NULL DEFAULT 1,
    start_time       VARCHAR(512)  NOT NULL,
    end_time         VARCHAR(512)  NOT NULL,
    event_type       INT           NOT NULL DEFAULT 0,
    recurrence       VARCHAR(512),
    status           INT           NOT NULL DEFAULT 0,
    free_busy_status INT           NOT NULL DEFAULT 0,
    is_exception     INT           NOT NULL DEFAULT 0,
    recurring_event_id VARCHAR(64),
    reminders        VARCHAR(1024),
    location         VARCHAR(1024),
    gmt_create       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    gmt_modify       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_event_id ON event(event_id);
CREATE INDEX IF NOT EXISTS idx_calendar_id ON event(calendar_id);
CREATE INDEX IF NOT EXISTS idx_event_type ON event(event_type);
CREATE INDEX IF NOT EXISTS idx_status_event ON event(status);
CREATE INDEX IF NOT EXISTS idx_recurring_event_id ON event(recurring_event_id);
CREATE INDEX IF NOT EXISTS idx_calendar_status ON event(calendar_id, status);
CREATE INDEX IF NOT EXISTS idx_calendar_type ON event(calendar_id, event_type);


-- =====================================================
-- 3. Attendee Table (日程参与人表)
-- =====================================================

CREATE TABLE IF NOT EXISTS attendee (
    id               BIGINT        NOT NULL AUTO_INCREMENT PRIMARY KEY,
    attendee_id      VARCHAR(64)   NOT NULL DEFAULT '',
    event_id         VARCHAR(64)   NOT NULL DEFAULT '',
    customer_id      VARCHAR(64)   NOT NULL DEFAULT '',
    role             INT           NOT NULL DEFAULT 0,
    rsvp_status      INT           NOT NULL DEFAULT 0,
    gmt_create       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_attendee_id ON attendee(attendee_id);
CREATE INDEX IF NOT EXISTS idx_event_id ON attendee(event_id);
CREATE INDEX IF NOT EXISTS idx_customer_id ON attendee(customer_id);
CREATE INDEX IF NOT EXISTS idx_rsvp_status ON attendee(rsvp_status);
CREATE INDEX IF NOT EXISTS idx_event_customer ON attendee(event_id, customer_id);
CREATE INDEX IF NOT EXISTS idx_event_role ON attendee(event_id, role);
CREATE UNIQUE INDEX IF NOT EXISTS uk_event_customer ON attendee(event_id, customer_id);
