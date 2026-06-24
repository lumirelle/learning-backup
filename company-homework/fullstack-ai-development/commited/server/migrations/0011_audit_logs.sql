-- 接口级操作日志（中间件写入）
CREATE TABLE IF NOT EXISTS audit_logs (
    id         BIGSERIAL PRIMARY KEY,
    user_id    BIGINT NOT NULL DEFAULT 0,
    username   VARCHAR(64) NOT NULL DEFAULT '',
    method     VARCHAR(16) NOT NULL DEFAULT '',
    path       VARCHAR(256) NOT NULL DEFAULT '',
    status     INT NOT NULL DEFAULT 0,
    ip         VARCHAR(64) NOT NULL DEFAULT '',
    request_id VARCHAR(64) NOT NULL DEFAULT '',
    body       TEXT NOT NULL DEFAULT '',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_audit_logs_user ON audit_logs(user_id);
CREATE INDEX IF NOT EXISTS idx_audit_logs_created ON audit_logs(created_at);
CREATE INDEX IF NOT EXISTS idx_audit_logs_path ON audit_logs(path);
