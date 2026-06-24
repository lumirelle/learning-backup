-- SSO 登录：users 绑定唯一 ID
-- 仅做加列，不改动既有数据；空串表示未绑定。
ALTER TABLE users ADD COLUMN IF NOT EXISTS wx_uid VARCHAR(64) NOT NULL DEFAULT '';
CREATE UNIQUE INDEX IF NOT EXISTS uq_users_wx_uid ON users(wx_uid) WHERE wx_uid <> '' AND deleted_at IS NULL;
