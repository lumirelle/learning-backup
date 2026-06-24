-- 登录账号（与 employees 解耦）
CREATE TABLE IF NOT EXISTS users (
    id            BIGINT PRIMARY KEY,
    username      VARCHAR(64) NOT NULL,
    name          VARCHAR(64) NOT NULL,
    password_hash VARCHAR(256) NOT NULL,
    employee_id   BIGINT NULL,
    email         VARCHAR(128) NOT NULL DEFAULT '',
    phone         VARCHAR(32) NOT NULL DEFAULT '',
    avatar        VARCHAR(256) NOT NULL DEFAULT '',
    org_id        BIGINT NOT NULL DEFAULT 0,
    org_path      VARCHAR(512) NOT NULL DEFAULT '',
    roles         JSONB NOT NULL DEFAULT '[]'::jsonb,
    is_admin      BOOLEAN NOT NULL DEFAULT FALSE,
    is_active     BOOLEAN NOT NULL DEFAULT TRUE,
    last_login_at TIMESTAMPTZ NULL,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at    TIMESTAMPTZ NULL
);
CREATE UNIQUE INDEX IF NOT EXISTS uq_users_username ON users(username) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_users_org_path ON users(org_path);
CREATE INDEX IF NOT EXISTS idx_users_roles ON users USING gin(roles);
