-- 职级序列（P 专业 / M 管理）
CREATE TABLE IF NOT EXISTS job_levels (
    id          BIGINT PRIMARY KEY,
    seq_code    VARCHAR(16) NOT NULL,
    name        VARCHAR(64) NOT NULL,
    level_code  VARCHAR(16) NOT NULL,
    level_order INT NOT NULL DEFAULT 0,
    description TEXT NOT NULL DEFAULT '',
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE UNIQUE INDEX IF NOT EXISTS uq_job_levels_seq_code ON job_levels(seq_code, level_code);
