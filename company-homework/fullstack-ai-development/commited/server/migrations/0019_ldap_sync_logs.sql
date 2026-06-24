-- LDAP 同步操作记录：每次同步（手动/定时）落一行，供组织页展示「上次同步 + 历史」。
CREATE TABLE IF NOT EXISTS ldap_sync_logs (
    id           BIGINT PRIMARY KEY,
    trigger      VARCHAR(16) NOT NULL,          -- manual / scheduled / cli
    operator_id  BIGINT NOT NULL DEFAULT 0,     -- 触发人（定时为 0）
    operator     VARCHAR(64) NOT NULL DEFAULT '',
    status       VARCHAR(16) NOT NULL,          -- success / failed
    dry_run      BOOLEAN NOT NULL DEFAULT FALSE,
    orgs         INT NOT NULL DEFAULT 0,        -- created+updated 概要
    depts        INT NOT NULL DEFAULT 0,
    employees    INT NOT NULL DEFAULT 0,
    users        INT NOT NULL DEFAULT 0,
    deactivated  INT NOT NULL DEFAULT 0,        -- 收敛软删总数
    detail       JSONB NOT NULL DEFAULT '{}'::jsonb, -- 完整 SyncReport
    message      TEXT NOT NULL DEFAULT '',      -- 失败原因
    duration_ms  BIGINT NOT NULL DEFAULT 0,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_ldap_sync_logs_created ON ldap_sync_logs(created_at DESC);
