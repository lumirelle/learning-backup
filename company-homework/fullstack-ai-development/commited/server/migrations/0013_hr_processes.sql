-- 统一人事流程单：入职/离职/调动/转正
CREATE TABLE IF NOT EXISTS hr_processes (
    id             BIGINT PRIMARY KEY,
    process_no     VARCHAR(64) NOT NULL,
    type           VARCHAR(32) NOT NULL,          -- onboard|offboard|transfer|regularize
    employee_id    BIGINT NULL,
    applicant_id   BIGINT NOT NULL DEFAULT 0,
    org_id         BIGINT NOT NULL DEFAULT 0,
    org_path       VARCHAR(512) NOT NULL DEFAULT '',
    payload        JSONB NOT NULL DEFAULT '{}'::jsonb,
    status         VARCHAR(16) NOT NULL DEFAULT 'pending', -- pending|approved|rejected|effective|cancelled
    effective_date DATE NULL,
    result         JSONB NOT NULL DEFAULT '{}'::jsonb,
    approval_id    BIGINT NULL,
    created_at     TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE UNIQUE INDEX IF NOT EXISTS uq_hr_processes_no ON hr_processes(process_no);
CREATE INDEX IF NOT EXISTS idx_hr_processes_type ON hr_processes(type);
CREATE INDEX IF NOT EXISTS idx_hr_processes_status ON hr_processes(status);
CREATE INDEX IF NOT EXISTS idx_hr_processes_emp ON hr_processes(employee_id);
CREATE INDEX IF NOT EXISTS idx_hr_processes_applicant ON hr_processes(applicant_id);
