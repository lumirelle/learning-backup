-- 岗位
CREATE TABLE IF NOT EXISTS positions (
    id         BIGINT PRIMARY KEY,
    org_id     BIGINT NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    dept_id    BIGINT NULL REFERENCES departments(id) ON DELETE SET NULL,
    code       VARCHAR(64) NOT NULL,
    name       VARCHAR(128) NOT NULL,
    job_family VARCHAR(64) NOT NULL DEFAULT '',
    level_seq  VARCHAR(16) NOT NULL DEFAULT '',
    headcount  INT NOT NULL DEFAULT 0,
    status     VARCHAR(16) NOT NULL DEFAULT 'active',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMPTZ NULL
);
CREATE UNIQUE INDEX IF NOT EXISTS uq_positions_dept_code ON positions(dept_id, code) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_positions_dept ON positions(dept_id);
