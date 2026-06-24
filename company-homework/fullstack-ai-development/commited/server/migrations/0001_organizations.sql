-- 组织主体树（集团/子公司/BU）
CREATE TABLE IF NOT EXISTS organizations (
    id          BIGINT PRIMARY KEY,
    parent_id   BIGINT NULL REFERENCES organizations(id) ON DELETE SET NULL,
    code        VARCHAR(64) NOT NULL,
    name        VARCHAR(128) NOT NULL,
    short_name  VARCHAR(64) NOT NULL DEFAULT '',
    type        VARCHAR(32) NOT NULL DEFAULT 'group',
    path        VARCHAR(512) NOT NULL,
    sort_order  INT NOT NULL DEFAULT 0,
    status      VARCHAR(16) NOT NULL DEFAULT 'active',
    description TEXT NOT NULL DEFAULT '',
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at  TIMESTAMPTZ NULL
);
CREATE UNIQUE INDEX IF NOT EXISTS uq_organizations_code ON organizations(code) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_organizations_parent ON organizations(parent_id);
CREATE INDEX IF NOT EXISTS idx_organizations_path ON organizations(path);
