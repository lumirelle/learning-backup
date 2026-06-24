-- 数据字典（学历/婚姻/合同类型/离职原因 ...）
CREATE TABLE IF NOT EXISTS data_dicts (
    id         BIGINT PRIMARY KEY,
    category   VARCHAR(64) NOT NULL,
    code       VARCHAR(64) NOT NULL,
    label      VARCHAR(128) NOT NULL,
    sort_order INT NOT NULL DEFAULT 0,
    enabled    BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE UNIQUE INDEX IF NOT EXISTS uq_data_dicts_cat_code ON data_dicts(category, code);
