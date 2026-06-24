-- 任职履历快照（调动/转正/入离职）
CREATE TABLE IF NOT EXISTS employee_job_history (
    id             BIGINT PRIMARY KEY,
    employee_id    BIGINT NOT NULL REFERENCES employees(id) ON DELETE CASCADE,
    change_type    VARCHAR(32) NOT NULL,
    from_snapshot  JSONB NOT NULL DEFAULT '{}'::jsonb,
    to_snapshot    JSONB NOT NULL DEFAULT '{}'::jsonb,
    process_id     BIGINT NULL,
    effective_date DATE NULL,
    remark         TEXT NOT NULL DEFAULT '',
    created_at     TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_job_history_emp ON employee_job_history(employee_id);
