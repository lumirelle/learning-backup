-- LDAP 组织/人员同步：给组织/部门/人员/账号加「外部来源」隔离列。
-- ext_source='ldap' 标记由 LDAP 同步维护的行，ext_id 为其在 LDAP 的稳定键
-- （部门=entryUUID 或 path:<路径>，人员=wxUid）。既有自建/业务数据 ext_source 为空，
-- 同步只 upsert/收敛自己来源的行，互不干扰。纯加列，零数据改写。
ALTER TABLE organizations ADD COLUMN IF NOT EXISTS ext_source VARCHAR(16) NOT NULL DEFAULT '';
ALTER TABLE organizations ADD COLUMN IF NOT EXISTS ext_id     VARCHAR(128) NOT NULL DEFAULT '';
ALTER TABLE departments   ADD COLUMN IF NOT EXISTS ext_source VARCHAR(16) NOT NULL DEFAULT '';
ALTER TABLE departments   ADD COLUMN IF NOT EXISTS ext_id     VARCHAR(128) NOT NULL DEFAULT '';
ALTER TABLE employees     ADD COLUMN IF NOT EXISTS ext_source VARCHAR(16) NOT NULL DEFAULT '';
ALTER TABLE employees     ADD COLUMN IF NOT EXISTS ext_id     VARCHAR(128) NOT NULL DEFAULT '';
ALTER TABLE users         ADD COLUMN IF NOT EXISTS ext_source VARCHAR(16) NOT NULL DEFAULT '';
ALTER TABLE users         ADD COLUMN IF NOT EXISTS ext_id     VARCHAR(128) NOT NULL DEFAULT '';

-- 同源 ext_id 唯一（仅对非空来源生效），保证 upsert 幂等。
CREATE UNIQUE INDEX IF NOT EXISTS uq_org_ext  ON organizations(ext_source, ext_id) WHERE ext_source <> '' AND deleted_at IS NULL;
CREATE UNIQUE INDEX IF NOT EXISTS uq_dept_ext ON departments(ext_source, ext_id)   WHERE ext_source <> '' AND deleted_at IS NULL;
CREATE UNIQUE INDEX IF NOT EXISTS uq_emp_ext  ON employees(ext_source, ext_id)     WHERE ext_source <> '' AND deleted_at IS NULL;
CREATE UNIQUE INDEX IF NOT EXISTS uq_user_ext ON users(ext_source, ext_id)         WHERE ext_source <> '' AND deleted_at IS NULL;
