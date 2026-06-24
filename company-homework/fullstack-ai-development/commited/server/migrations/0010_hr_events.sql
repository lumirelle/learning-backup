-- 人事动态时间线（业务语义事件）
CREATE TABLE IF NOT EXISTS hr_events (
    id          BIGINT PRIMARY KEY,
    employee_id BIGINT NULL,
    event_type  VARCHAR(32) NOT NULL,
    title       VARCHAR(256) NOT NULL,
    detail      JSONB NOT NULL DEFAULT '{}'::jsonb,
    org_id      BIGINT NOT NULL DEFAULT 0,
    org_path    VARCHAR(512) NOT NULL DEFAULT '',
    actor_id    BIGINT NULL,
    occurred_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_hr_events_emp ON hr_events(employee_id);
CREATE INDEX IF NOT EXISTS idx_hr_events_type ON hr_events(event_type);
CREATE INDEX IF NOT EXISTS idx_hr_events_occurred ON hr_events(occurred_at);
