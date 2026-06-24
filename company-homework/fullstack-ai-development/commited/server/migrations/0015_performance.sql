-- 任职奖惩：奖励/惩罚记录（关联员工，进时间线）
CREATE TABLE IF NOT EXISTS rewards_punishments (
    id             BIGINT PRIMARY KEY,
    employee_id    BIGINT NOT NULL REFERENCES employees(id) ON DELETE CASCADE,
    kind           VARCHAR(16) NOT NULL,           -- reward | punishment
    category       VARCHAR(64) NOT NULL DEFAULT '', -- 嘉奖/记功/警告/记过 ...
    title          VARCHAR(256) NOT NULL,
    reason         TEXT NOT NULL DEFAULT '',
    amount         NUMERIC(12,2) NULL,             -- 涉及金额（可空）
    effective_date DATE NULL,
    recorded_by    BIGINT NOT NULL DEFAULT 0,
    created_at     TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at     TIMESTAMPTZ NULL
);
CREATE INDEX IF NOT EXISTS idx_rewpun_emp ON rewards_punishments(employee_id);
CREATE INDEX IF NOT EXISTS idx_rewpun_kind ON rewards_punishments(kind);
