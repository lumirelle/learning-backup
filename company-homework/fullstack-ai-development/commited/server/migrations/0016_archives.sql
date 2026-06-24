-- 档案库：分类 / 档案条目 / 借阅单（借阅走通用审批引擎）
CREATE TABLE IF NOT EXISTS archive_categories (
    id         BIGINT PRIMARY KEY,
    parent_id  BIGINT NULL,
    name       VARCHAR(128) NOT NULL,
    code       VARCHAR(64) NOT NULL DEFAULT '',
    path       VARCHAR(512) NOT NULL DEFAULT '',
    sort_order INT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS archive_items (
    id             BIGINT PRIMARY KEY,
    employee_id    BIGINT NULL REFERENCES employees(id) ON DELETE SET NULL,
    category_id    BIGINT NULL,
    title          VARCHAR(256) NOT NULL,
    file_meta      JSONB NOT NULL DEFAULT '{}'::jsonb, -- 文件名/大小/类型（演示用，无真实存储）
    storage_ref    VARCHAR(256) NOT NULL DEFAULT '',
    is_borrowable  BOOLEAN NOT NULL DEFAULT TRUE,
    status         VARCHAR(16) NOT NULL DEFAULT 'in_stock', -- in_stock | borrowed
    security_level VARCHAR(16) NOT NULL DEFAULT 'normal',
    created_at     TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at     TIMESTAMPTZ NULL
);
CREATE INDEX IF NOT EXISTS idx_archive_items_emp ON archive_items(employee_id);
CREATE INDEX IF NOT EXISTS idx_archive_items_cat ON archive_items(category_id);

CREATE TABLE IF NOT EXISTS archive_borrows (
    id          BIGINT PRIMARY KEY,
    item_id     BIGINT NOT NULL REFERENCES archive_items(id) ON DELETE CASCADE,
    borrower_id BIGINT NOT NULL DEFAULT 0,
    reason      TEXT NOT NULL DEFAULT '',
    status      VARCHAR(16) NOT NULL DEFAULT 'pending', -- pending|borrowed|returned|rejected
    approval_id BIGINT NULL,
    due_date    DATE NULL,
    borrowed_at TIMESTAMPTZ NULL,
    returned_at TIMESTAMPTZ NULL,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_archive_borrows_item ON archive_borrows(item_id);
CREATE INDEX IF NOT EXISTS idx_archive_borrows_status ON archive_borrows(status);
