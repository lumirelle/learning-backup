-- 合同模板
CREATE TABLE IF NOT EXISTS contract_templates (
    id         BIGINT PRIMARY KEY,
    name       VARCHAR(128) NOT NULL,
    type       VARCHAR(32) NOT NULL DEFAULT 'fixed_term',
    content    TEXT NOT NULL DEFAULT '',
    enabled    BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 劳动合同
CREATE TABLE IF NOT EXISTS contracts (
    id               BIGINT PRIMARY KEY,
    contract_no      VARCHAR(64) NOT NULL,
    employee_id      BIGINT NOT NULL REFERENCES employees(id) ON DELETE CASCADE,
    template_id      BIGINT NULL,
    type             VARCHAR(32) NOT NULL DEFAULT 'fixed_term', -- fixed_term|open_ended|intern|labor_dispatch
    status           VARCHAR(16) NOT NULL DEFAULT 'active',     -- draft|active|expired|terminated|renewed
    sign_date        DATE NULL,
    start_date       DATE NULL,
    end_date         DATE NULL,
    prev_contract_id BIGINT NULL,
    salary_band      VARCHAR(64) NOT NULL DEFAULT '',
    terms            JSONB NOT NULL DEFAULT '{}'::jsonb,
    created_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at       TIMESTAMPTZ NULL
);
CREATE UNIQUE INDEX IF NOT EXISTS uq_contracts_no ON contracts(contract_no) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_contracts_emp ON contracts(employee_id);
CREATE INDEX IF NOT EXISTS idx_contracts_status ON contracts(status);
CREATE INDEX IF NOT EXISTS idx_contracts_end ON contracts(end_date);
