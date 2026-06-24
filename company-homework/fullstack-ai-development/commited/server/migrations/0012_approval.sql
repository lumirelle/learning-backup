-- 通用审批引擎：实例 + 步骤（串行多级）
CREATE TABLE IF NOT EXISTS approval_instances (
    id           BIGINT PRIMARY KEY,
    biz_type     VARCHAR(32) NOT NULL,            -- process | borrow | promotion ...
    biz_id       BIGINT NOT NULL,
    status       VARCHAR(16) NOT NULL DEFAULT 'pending', -- pending|approved|rejected|cancelled
    current_step INT NOT NULL DEFAULT 1,
    total_steps  INT NOT NULL DEFAULT 1,
    submitted_by BIGINT NOT NULL DEFAULT 0,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_appr_inst_biz ON approval_instances(biz_type, biz_id);
CREATE INDEX IF NOT EXISTS idx_appr_inst_status ON approval_instances(status);

CREATE TABLE IF NOT EXISTS approval_steps (
    id          BIGINT PRIMARY KEY,
    instance_id BIGINT NOT NULL REFERENCES approval_instances(id) ON DELETE CASCADE,
    step_no     INT NOT NULL,
    approver_id BIGINT NOT NULL,
    action      VARCHAR(16) NOT NULL DEFAULT 'pending', -- pending|approved|rejected
    comment     TEXT NOT NULL DEFAULT '',
    acted_at    TIMESTAMPTZ NULL,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_appr_steps_inst ON approval_steps(instance_id);
CREATE INDEX IF NOT EXISTS idx_appr_steps_approver ON approval_steps(approver_id, action);
