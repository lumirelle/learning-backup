-- 部门树（隶属某组织）
CREATE TABLE IF NOT EXISTS departments (
    id           BIGINT PRIMARY KEY,
    org_id       BIGINT NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    parent_id    BIGINT NULL REFERENCES departments(id) ON DELETE SET NULL,
    code         VARCHAR(64) NOT NULL,
    name         VARCHAR(128) NOT NULL,
    short_name   VARCHAR(64) NOT NULL DEFAULT '',
    path         VARCHAR(512) NOT NULL,
    sort_order   INT NOT NULL DEFAULT 0,
    head_user_id BIGINT NULL,
    status       VARCHAR(16) NOT NULL DEFAULT 'active',
    description  TEXT NOT NULL DEFAULT '',
    created_at   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at   TIMESTAMPTZ NULL
);
CREATE UNIQUE INDEX IF NOT EXISTS uq_departments_org_code ON departments(org_id, code) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_departments_org ON departments(org_id);
CREATE INDEX IF NOT EXISTS idx_departments_parent ON departments(parent_id);
CREATE INDEX IF NOT EXISTS idx_departments_path ON departments(path);
