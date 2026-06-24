-- 人事档案主表（花名册）
CREATE TABLE IF NOT EXISTS employees (
    id                BIGINT PRIMARY KEY,
    employee_no       VARCHAR(64) NOT NULL,
    user_id           BIGINT NULL,
    name              VARCHAR(64) NOT NULL,
    en_name           VARCHAR(64) NOT NULL DEFAULT '',
    gender            VARCHAR(16) NOT NULL DEFAULT '',
    birthday          DATE NULL,
    avatar            VARCHAR(256) NOT NULL DEFAULT '',
    phone             VARCHAR(32) NOT NULL DEFAULT '',
    work_email        VARCHAR(128) NOT NULL DEFAULT '',
    education         VARCHAR(32) NOT NULL DEFAULT '',
    org_id            BIGINT NOT NULL DEFAULT 0,
    org_path          VARCHAR(512) NOT NULL DEFAULT '',
    dept_id           BIGINT NULL,
    dept_name         VARCHAR(128) NOT NULL DEFAULT '',
    position_id       BIGINT NULL,
    position_name     VARCHAR(128) NOT NULL DEFAULT '',
    job_level         VARCHAR(16) NOT NULL DEFAULT '',
    manager_id        BIGINT NULL,
    employment_type   VARCHAR(32) NOT NULL DEFAULT 'full_time',
    employment_status VARCHAR(32) NOT NULL DEFAULT 'active',
    hired_at          DATE NULL,
    regular_at        DATE NULL,
    left_at           DATE NULL,
    created_at        TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at        TIMESTAMPTZ NULL
);
CREATE UNIQUE INDEX IF NOT EXISTS uq_employees_no ON employees(employee_no) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_employees_org_path ON employees(org_path);
CREATE INDEX IF NOT EXISTS idx_employees_dept ON employees(dept_id);
CREATE INDEX IF NOT EXISTS idx_employees_status ON employees(employment_status);
CREATE INDEX IF NOT EXISTS idx_employees_name ON employees(name);
