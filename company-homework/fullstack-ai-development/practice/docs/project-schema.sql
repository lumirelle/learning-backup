-- ============================================================
--  ATS 招聘管理系统 — 数据库 Schema
--  数据库：PostgreSQL 16
--  版本：v1.1   日期：2026-05-22
--  变更：M2 引入 jobs 5 态状态机 + tags 标准化表 + 组合拳搜索索引
-- ============================================================

-- ────────────────────────────────────────────────────────────
--  扩展（必须在 superuser 下安装；Postgres 16 默认账号即 superuser）
-- ────────────────────────────────────────────────────────────

CREATE EXTENSION IF NOT EXISTS pg_trgm;     -- title ILIKE 加速（GIN trgm）

-- ────────────────────────────────────────────────────────────
--  枚举类型
-- ────────────────────────────────────────────────────────────

-- 用户角色
CREATE TYPE user_role AS ENUM (
    'ADMIN',      -- 超级管理员
    'HR',         -- HR / 招聘专员
    'CANDIDATE'   -- 候选人
);

-- 岗位状态（5 态状态机，合法流转见 JobStatusMachine.java）
--   DRAFT     ──publish──▶ PUBLISHED ──pause───▶ PAUSED
--   DRAFT     ──archive──▶ ARCHIVED  ──restore─▶ DRAFT
--   PUBLISHED ──close────▶ CLOSED    ──archive─▶ ARCHIVED
--   PAUSED    ──publish──▶ PUBLISHED （恢复招聘）
--   PAUSED    ──close────▶ CLOSED    （直接关闭）
CREATE TYPE job_status AS ENUM (
    'DRAFT',       -- 草稿，HR 编辑中，候选人不可见
    'PUBLISHED',   -- 招聘中，候选人可见可投递
    'PAUSED',      -- 暂停收件（HR 短期不想看新简历，候选人仍可见但提示暂停）
    'CLOSED',      -- 已关闭，候选人可见标记已关闭，不可投递
    'ARCHIVED'     -- 已归档，列表默认隐藏，可恢复为 DRAFT
);

-- 工作类型
CREATE TYPE job_work_type AS ENUM (
    'FULL_TIME',   -- 全职
    'PART_TIME',   -- 兼职
    'CONTRACT',    -- 合同制
    'INTERN',      -- 实习
    'REMOTE'       -- 远程
);

-- 岗位资历级别
CREATE TYPE job_level AS ENUM (
    'INTERN',      -- 实习
    'JUNIOR',      -- 初级 (P4/L3)
    'MID',         -- 中级 (P5/L4)
    'SENIOR',      -- 高级 (P6/L5)
    'LEAD',        -- 资深 / 团队 Lead
    'DIRECTOR'     -- 总监
);

-- 标签分类
CREATE TYPE tag_category AS ENUM (
    'TECH',        -- 技术栈：Java / Spring / TypeScript
    'SOFT',        -- 软实力：沟通 / 团队协作
    'CERT',        -- 证书：CISSP / AWS Certified
    'LANG',        -- 语言能力：英语流利 / 日语 N1
    'DOMAIN'       -- 业务领域：金融 / 电商 / 医疗
);

-- 候选人申请阶段
CREATE TYPE application_stage AS ENUM (
    'APPLIED',           -- 待筛选（刚投递）
    'SCREENING_PASS',    -- 简历通过
    'PHONE_INTERVIEW',   -- 电话面试
    'TECH_INTERVIEW',    -- 技术面试
    'HR_INTERVIEW',      -- HR 终面
    'OFFER',             -- 发放 Offer
    'HIRED',             -- 已入职（终态）
    'REJECTED'           -- 已拒绝（终态）
);

-- 面试结论
CREATE TYPE interview_conclusion AS ENUM (
    'PASS',    -- 通过
    'REJECT',  -- 拒绝
    'HOLD'     -- 待定
);

-- ────────────────────────────────────────────────────────────
--  通用触发器函数：自动更新 updated_at
-- ────────────────────────────────────────────────────────────

CREATE OR REPLACE FUNCTION set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- ────────────────────────────────────────────────────────────
--  1. users — 用户表
-- ────────────────────────────────────────────────────────────

CREATE TABLE users (
    id            BIGSERIAL       PRIMARY KEY,
    email         VARCHAR(255)    NOT NULL,
    password_hash VARCHAR(255)    NOT NULL,                  -- BCrypt hash，禁止明文
    full_name     VARCHAR(100)    NOT NULL,
    role          user_role       NOT NULL DEFAULT 'CANDIDATE',
    is_active     BOOLEAN         NOT NULL DEFAULT TRUE,     -- 软删除/禁用标志
    candidate_interests TEXT,                              -- 候选人兴趣标签 JSON 数组，如 ["fe","pm"]
    created_at    TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    CONSTRAINT users_email_unique UNIQUE (email)
);

COMMENT ON TABLE  users                IS '系统用户：包含管理员、HR 和候选人三种角色';
COMMENT ON COLUMN users.id            IS '自增主键';
COMMENT ON COLUMN users.email         IS '登录邮箱，全局唯一';
COMMENT ON COLUMN users.password_hash IS 'BCrypt(cost=12) 加密后的密码';
COMMENT ON COLUMN users.role          IS '角色：ADMIN / HR / CANDIDATE';
COMMENT ON COLUMN users.is_active     IS 'FALSE 表示账号已禁用';

-- 索引
CREATE INDEX idx_users_email    ON users (email);
CREATE INDEX idx_users_role     ON users (role);

-- 自动更新 updated_at
CREATE TRIGGER trg_users_updated_at
    BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- ────────────────────────────────────────────────────────────
--  2a. root_orgs — 根组织表（M6：组织树根节点，固定单行）
-- ────────────────────────────────────────────────────────────

CREATE TABLE root_orgs (
    id         BIGSERIAL     PRIMARY KEY,
    name       VARCHAR(100)  NOT NULL,
    created_at TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ   NOT NULL DEFAULT NOW(),

    CONSTRAINT root_orgs_name_unique UNIQUE (name)
);

COMMENT ON TABLE  root_orgs      IS '组织树根节点（M6）。业务上固定单行：「xx科技集团」。';
COMMENT ON COLUMN root_orgs.name IS '集团/组织名称，全局唯一。前端展示为树根，不可编辑。';

CREATE TRIGGER trg_root_orgs_updated_at
    BEFORE UPDATE ON root_orgs
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- ────────────────────────────────────────────────────────────
--  2b. departments — 部门表（M6 改造：作为「中间节点」，可嵌套）
--      - 直挂 root：parent_department_id IS NULL（root_org_id 必填）
--      - 嵌套部门：parent_department_id 指向另一 dept（root_org_id 仍要一致）
--      - 部门节点本身不关联 HR / 岗位 / 工作地点，那是 sub_departments 的事
-- ────────────────────────────────────────────────────────────

CREATE TABLE departments (
    id                    BIGSERIAL     PRIMARY KEY,
    root_org_id           BIGINT        NOT NULL REFERENCES root_orgs(id) ON DELETE RESTRICT,
    parent_department_id  BIGINT        REFERENCES departments(id) ON DELETE RESTRICT,
    name                  VARCHAR(100)  NOT NULL,
    created_at            TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    updated_at            TIMESTAMPTZ   NOT NULL DEFAULT NOW(),

    -- 同一直接父节点下名称唯一（root + 同名 dept 也算冲突）
    CONSTRAINT departments_parent_name_unique UNIQUE (root_org_id, parent_department_id, name)
);

COMMENT ON TABLE  departments                      IS '组织树中间节点（M6）。容纳子 departments 或 sub_departments。';
COMMENT ON COLUMN departments.root_org_id          IS 'FK → root_orgs。每个部门必属一个根组织。';
COMMENT ON COLUMN departments.parent_department_id IS 'FK → departments。NULL = 直挂 root；非空 = 嵌套子部门。';
COMMENT ON COLUMN departments.name                 IS '部门名称，在同一直接父节点下唯一。';

CREATE INDEX idx_departments_root_org_id   ON departments (root_org_id);
CREATE INDEX idx_departments_parent_id     ON departments (parent_department_id);

CREATE TRIGGER trg_departments_updated_at
    BEFORE UPDATE ON departments
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- ────────────────────────────────────────────────────────────
--  2c. sub_departments — 子部门表（M6：组织树叶子节点）
--      - 必挂在某个 department 下（不允许直挂 root）
--      - 子部门没有子节点，但**关联 HR + 岗位 + 工作地点**
--      - 工作地点字段从 jobs 表迁移过来，jobs.location 已 DROP（M6）
-- ────────────────────────────────────────────────────────────

CREATE TABLE sub_departments (
    id                    BIGSERIAL     PRIMARY KEY,
    parent_department_id  BIGINT        NOT NULL REFERENCES departments(id) ON DELETE RESTRICT,
    name                  VARCHAR(100)  NOT NULL,
    location              VARCHAR(200)  NOT NULL,
    created_at            TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    updated_at            TIMESTAMPTZ   NOT NULL DEFAULT NOW(),

    CONSTRAINT sub_departments_parent_name_unique UNIQUE (parent_department_id, name)
);

COMMENT ON TABLE  sub_departments                      IS '组织树叶子节点（M6）。挂 HR、岗位、工作地点。';
COMMENT ON COLUMN sub_departments.parent_department_id IS 'FK → departments。必填，子部门必有上层部门。';
COMMENT ON COLUMN sub_departments.name                 IS '子部门名称，在同一父部门下唯一。建议命名「部门-地点」。';
COMMENT ON COLUMN sub_departments.location             IS '工作地点。原 jobs.location 字段下沉到此（M6）。';

CREATE INDEX idx_sub_departments_parent_id ON sub_departments (parent_department_id);

CREATE TRIGGER trg_sub_departments_updated_at
    BEFORE UPDATE ON sub_departments
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- ────────────────────────────────────────────────────────────
--  2d. hr_sub_departments — HR ↔ 子部门 多对多关联表（M6）
--      - 一个 HR 可同时服务多个子部门（跨地点 / 跨产品线）
--      - 删 user CASCADE 同步清；删 sub_department 阻止（防孤儿）
-- ────────────────────────────────────────────────────────────

CREATE TABLE hr_sub_departments (
    user_id            BIGINT      NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    sub_department_id  BIGINT      NOT NULL REFERENCES sub_departments(id) ON DELETE RESTRICT,
    created_at         TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    PRIMARY KEY (user_id, sub_department_id)
);

COMMENT ON TABLE hr_sub_departments IS 'HR 用户与子部门的多对多绑定（M6）。仅 HR 角色用户出现在此表。';

CREATE INDEX idx_hr_sub_departments_sub_dept_id ON hr_sub_departments (sub_department_id);

-- ────────────────────────────────────────────────────────────
--  3. jobs — 岗位表（M2 增强：5 态状态机 + 结构化薪资 + 浏览量 + 软删）
-- ────────────────────────────────────────────────────────────

CREATE TABLE jobs (
    id                BIGSERIAL       PRIMARY KEY,
    -- M6 改造：去掉旧的 department_id（指向中间节点），改为指向 sub_departments
    -- 工作地点字段下沉到 sub_departments.location，jobs 表不再持有 location 列
    sub_department_id BIGINT          NOT NULL REFERENCES sub_departments(id) ON DELETE RESTRICT,
    created_by        BIGINT          NOT NULL REFERENCES users(id) ON DELETE RESTRICT,

    title             VARCHAR(200)    NOT NULL,
    description       TEXT,                                       -- 岗位 JD，支持 Markdown
    work_type         job_work_type   NOT NULL DEFAULT 'FULL_TIME',
    level             job_level       NOT NULL DEFAULT 'MID',

    salary_min        INTEGER         CHECK (salary_min >= 0),    -- 月薪下限（元），NULL 表示面议
    salary_max        INTEGER         CHECK (salary_max >= 0),    -- 月薪上限（元）
    headcount         SMALLINT        NOT NULL DEFAULT 1 CHECK (headcount > 0),

    status            job_status      NOT NULL DEFAULT 'DRAFT',
    view_count        INTEGER         NOT NULL DEFAULT 0 CHECK (view_count >= 0),
    published_at      TIMESTAMPTZ,                                -- 首次进入 PUBLISHED 的时间
    closed_at         TIMESTAMPTZ,                                -- 进入 CLOSED 的时间

    created_at        TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    deleted_at        TIMESTAMPTZ,                                -- 软删，Admin 专用

    CONSTRAINT jobs_salary_range_chk
        CHECK (salary_min IS NULL OR salary_max IS NULL OR salary_min <= salary_max)
);

COMMENT ON TABLE  jobs                   IS '招聘岗位，5 态状态机由 JobStatusMachine 控制';
COMMENT ON COLUMN jobs.sub_department_id IS 'FK → sub_departments。M6 起岗位必须挂在子部门（叶子节点），不可删除。';
COMMENT ON COLUMN jobs.created_by        IS 'FK → users，创建人（HR），不可删除';
COMMENT ON COLUMN jobs.headcount         IS '招聘人数，至少为 1';
COMMENT ON COLUMN jobs.salary_min        IS '月薪下限（元/月），NULL 表示薪资面议';
COMMENT ON COLUMN jobs.salary_max        IS '月薪上限（元/月），≥ salary_min';
COMMENT ON COLUMN jobs.status            IS 'DRAFT/PUBLISHED/PAUSED/CLOSED/ARCHIVED，流转规则见 JobStatusMachine';
COMMENT ON COLUMN jobs.view_count        IS '岗位详情页浏览次数，候选人查看时 +1';
COMMENT ON COLUMN jobs.published_at      IS '首次发布时间，用于「最新岗位」排序';
COMMENT ON COLUMN jobs.deleted_at        IS '软删时间，Admin 物理删除入口在后台；候选人列表完全过滤';

-- 索引（带 WHERE deleted_at IS NULL 的 partial index 仅命中活跃岗位，体积更小）
CREATE INDEX idx_jobs_status            ON jobs (status)                              WHERE deleted_at IS NULL;
CREATE INDEX idx_jobs_sub_department_id ON jobs (sub_department_id)                   WHERE deleted_at IS NULL;
CREATE INDEX idx_jobs_created_by        ON jobs (created_by)                          WHERE deleted_at IS NULL;
CREATE INDEX idx_jobs_published_at      ON jobs (published_at DESC NULLS LAST)        WHERE deleted_at IS NULL;
CREATE INDEX idx_jobs_work_type         ON jobs (work_type)                           WHERE deleted_at IS NULL;
CREATE INDEX idx_jobs_level             ON jobs (level)                               WHERE deleted_at IS NULL;

-- 全文搜索组合拳：
--   (1) title 走 pg_trgm GIN，加速 ILIKE '%kw%' 模糊匹配（中文/英文都行）
--   (2) description 走 to_tsvector('english') GIN，英文岗位 JD 自动分词
CREATE INDEX idx_jobs_title_trgm    ON jobs USING GIN (title gin_trgm_ops)         WHERE deleted_at IS NULL;
CREATE INDEX idx_jobs_desc_fts      ON jobs USING GIN (to_tsvector('english', coalesce(description, ''))) WHERE deleted_at IS NULL;

CREATE TRIGGER trg_jobs_updated_at
    BEFORE UPDATE ON jobs
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- ────────────────────────────────────────────────────────────
--  3a. tags — 技能/标签标准化表
-- ────────────────────────────────────────────────────────────

CREATE TABLE tags (
    id         BIGSERIAL     PRIMARY KEY,
    slug       VARCHAR(80)   NOT NULL,                       -- url-safe 标识，如 'spring-boot'
    name       VARCHAR(80)   NOT NULL,                       -- 显示名，如 'Spring Boot'
    category   tag_category  NOT NULL DEFAULT 'TECH',
    created_at TIMESTAMPTZ   NOT NULL DEFAULT NOW(),

    CONSTRAINT tags_slug_unique UNIQUE (slug)
);

COMMENT ON TABLE  tags          IS '技能/标签标准化表，HR 选已有标签，避免拼写发散';
COMMENT ON COLUMN tags.slug     IS 'url-safe 唯一标识，前端 query string 用';
COMMENT ON COLUMN tags.category IS 'TECH(技术) / SOFT(软实力) / CERT(证书) / LANG(语言) / DOMAIN(业务领域)';

CREATE INDEX idx_tags_category ON tags (category);

-- ────────────────────────────────────────────────────────────
--  3b. job_tags — 岗位 ↔ 标签 关联表
-- ────────────────────────────────────────────────────────────

CREATE TABLE job_tags (
    job_id     BIGINT       NOT NULL REFERENCES jobs(id) ON DELETE CASCADE,
    tag_id     BIGINT       NOT NULL REFERENCES tags(id) ON DELETE RESTRICT,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT NOW(),

    PRIMARY KEY (job_id, tag_id)
);

COMMENT ON TABLE job_tags IS '岗位 ↔ 标签 多对多关联，查询时 JOIN tags 即可';

-- 反向查询索引（"有 Java 标签的岗位"用得到）
CREATE INDEX idx_job_tags_tag_id ON job_tags (tag_id);

-- ────────────────────────────────────────────────────────────
--  4. applications — 候选人申请表
-- ────────────────────────────────────────────────────────────

CREATE TABLE applications (
    id             BIGSERIAL         PRIMARY KEY,
    job_id         BIGINT            NOT NULL REFERENCES jobs(id) ON DELETE RESTRICT,
    candidate_id   BIGINT            NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    stage          application_stage NOT NULL DEFAULT 'APPLIED',
    resume_url     VARCHAR(500),                    -- 简历 PDF 存储路径或 URL
    years_exp      SMALLINT          CHECK (years_exp >= 0),
    phone          VARCHAR(30),
    reject_reason  TEXT,                            -- 拒绝原因，仅 REJECTED 阶段时填写
    applied_at     TIMESTAMPTZ       NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMPTZ       NOT NULL DEFAULT NOW(),

    -- 同一候选人不能对同一岗位重复投递
    CONSTRAINT applications_job_candidate_unique UNIQUE (job_id, candidate_id)
);

COMMENT ON TABLE  applications              IS '候选人对岗位的申请记录，核心业务实体';
COMMENT ON COLUMN applications.stage        IS '当前所处招聘阶段，状态机控制，不可回退';
COMMENT ON COLUMN applications.resume_url   IS '简历 PDF 的相对路径，如 /uploads/resumes/xxx.pdf';
COMMENT ON COLUMN applications.reject_reason IS '拒绝原因，HR 拒绝时必填';

-- 索引
CREATE INDEX idx_applications_job_id       ON applications (job_id);
CREATE INDEX idx_applications_candidate_id ON applications (candidate_id);
CREATE INDEX idx_applications_stage        ON applications (stage);
CREATE INDEX idx_applications_applied_at   ON applications (applied_at DESC);

CREATE TRIGGER trg_applications_updated_at
    BEFORE UPDATE ON applications
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- ────────────────────────────────────────────────────────────
--  5. stage_logs — 阶段流转审计日志
-- ────────────────────────────────────────────────────────────

CREATE TABLE stage_logs (
    id             BIGSERIAL         PRIMARY KEY,
    application_id BIGINT            NOT NULL REFERENCES applications(id) ON DELETE CASCADE,
    operated_by    BIGINT            NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    from_stage     application_stage,              -- NULL 表示初始投递
    to_stage       application_stage NOT NULL,
    note           TEXT,                           -- 操作备注（可选）
    operated_at    TIMESTAMPTZ       NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE  stage_logs               IS '候选人阶段流转的操作审计日志，只增不删';
COMMENT ON COLUMN stage_logs.from_stage    IS '流转前阶段，NULL 代表初始状态（投递时）';
COMMENT ON COLUMN stage_logs.to_stage      IS '流转后阶段';
COMMENT ON COLUMN stage_logs.operated_by   IS 'FK → users，执行流转操作的 HR/管理员';
COMMENT ON COLUMN stage_logs.note          IS '操作备注，拒绝时可记录简短原因';

-- 索引
CREATE INDEX idx_stage_logs_application_id ON stage_logs (application_id);
CREATE INDEX idx_stage_logs_operated_at    ON stage_logs (operated_at DESC);

-- ────────────────────────────────────────────────────────────
--  6. interview_records — 面试评价记录
-- ────────────────────────────────────────────────────────────

CREATE TABLE interview_records (
    id             BIGSERIAL            PRIMARY KEY,
    application_id BIGINT               NOT NULL REFERENCES applications(id) ON DELETE CASCADE,
    interviewer_id BIGINT               REFERENCES users(id) ON DELETE SET NULL,
    round          VARCHAR(100)         NOT NULL,  -- 面试轮次，如 "技术一面"、"HR 终面"
    rating         SMALLINT             CHECK (rating BETWEEN 1 AND 5),
    strengths      TEXT,                           -- 候选人优势
    weaknesses     TEXT,                           -- 候选人不足
    conclusion     interview_conclusion,           -- PASS / REJECT / HOLD
    notes          TEXT,                           -- 补充备注
    created_at     TIMESTAMPTZ          NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMPTZ          NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE  interview_records               IS '面试轮次评价记录，支持多轮存储';
COMMENT ON COLUMN interview_records.round         IS '面试轮次描述，如「技术一面」「HR 终面」';
COMMENT ON COLUMN interview_records.rating        IS '综合评分 1–5 星';
COMMENT ON COLUMN interview_records.conclusion    IS '面试结论：PASS / REJECT / HOLD';
COMMENT ON COLUMN interview_records.interviewer_id IS 'FK → users，面试官（允许账号被删除后保留记录）';

-- 索引
CREATE INDEX idx_interview_records_application_id ON interview_records (application_id);
CREATE INDEX idx_interview_records_interviewer_id ON interview_records (interviewer_id);
CREATE INDEX idx_interview_records_created_at     ON interview_records (created_at DESC);

CREATE TRIGGER trg_interview_records_updated_at
    BEFORE UPDATE ON interview_records
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- ────────────────────────────────────────────────────────────
--  7. refresh_tokens — JWT Refresh Token 存储
--     （Redis 优先；此表作为持久化备份或无 Redis 降级方案）
-- ────────────────────────────────────────────────────────────

CREATE TABLE refresh_tokens (
    id         BIGSERIAL    PRIMARY KEY,
    user_id    BIGINT       NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token_hash VARCHAR(255) NOT NULL,              -- SHA-256(token) 存储，避免泄露
    expires_at TIMESTAMPTZ  NOT NULL,
    revoked    BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT NOW(),

    CONSTRAINT refresh_tokens_hash_unique UNIQUE (token_hash)
);

COMMENT ON TABLE  refresh_tokens            IS 'JWT RefreshToken 持久化，支持主动吊销';
COMMENT ON COLUMN refresh_tokens.token_hash IS 'SHA-256(rawToken)，避免数据库泄露后 token 被直接使用';
COMMENT ON COLUMN refresh_tokens.revoked    IS 'TRUE 表示已主动登出/吊销';

-- 索引
CREATE INDEX idx_refresh_tokens_user_id    ON refresh_tokens (user_id);
CREATE INDEX idx_refresh_tokens_expires_at ON refresh_tokens (expires_at);

-- ────────────────────────────────────────────────────────────
--  种子数据：演示账号 & 示例部门
--
--  ⚠️ 三个演示账号<strong>共用同一密码 hash</strong>（cost=12 的 BCrypt 'Admin@123'）—
--      虽然 hash 字面值相同，但 BCrypt verify 不要求 hash 唯一，三个账号 login 都能通过；
--      这是<strong>仅 demo 可接受</strong>的简化，生产部署前必须为每个账号独立生成 hash。
--
--    ADMIN     admin@ats.local        / Admin@123    （System Admin，全局管理员）
--    HR        hr@ats.local           / Admin@123    （示例 HR，岗位创建者）
--    CANDIDATE candidate@ats.local    / Admin@123    （示例候选人，已投递 1 份）
--
--  生产部署前请用以下命令为每个账号重新生成 hash 并替换：
--    bun -e "console.log(await Bun.password.hash('YOUR_NEW_PWD', { algorithm: 'bcrypt', cost: 12 }))"
-- ────────────────────────────────────────────────────────────

INSERT INTO users (email, password_hash, full_name, role) VALUES
    ('admin@ats.local',
     '$2b$12$8daJQt9doR02lLfOVSmVWOW7e7DgA7pLAD39IgeqoTHdxmz0odHVm',
     'System Admin',
     'ADMIN'),
    ('hr@ats.local',
     '$2b$12$8daJQt9doR02lLfOVSmVWOW7e7DgA7pLAD39IgeqoTHdxmz0odHVm',
     '示例 HR · 张萌',
     'HR'),
    ('candidate@ats.local',
     '$2b$12$8daJQt9doR02lLfOVSmVWOW7e7DgA7pLAD39IgeqoTHdxmz0odHVm',
     '示例候选人 · 李哲',
     'CANDIDATE'),
    -- 额外的 5 个示例候选人（覆盖前端 / 后端 / 设计 / SRE / 产品五大方向，
    -- 让看板在多 stage 上有足够的真实分布。密码统一 Admin@123，仅 demo 用途）
    ('xiaotong.wang@demo.io',
     '$2b$12$8daJQt9doR02lLfOVSmVWOW7e7DgA7pLAD39IgeqoTHdxmz0odHVm',
     '王晓彤',
     'CANDIDATE'),
    ('haochen@demo.io',
     '$2b$12$8daJQt9doR02lLfOVSmVWOW7e7DgA7pLAD39IgeqoTHdxmz0odHVm',
     '陈昊',
     'CANDIDATE'),
    ('qingya.lin@demo.io',
     '$2b$12$8daJQt9doR02lLfOVSmVWOW7e7DgA7pLAD39IgeqoTHdxmz0odHVm',
     '林清雅',
     'CANDIDATE'),
    ('zixuan.liu@demo.io',
     '$2b$12$8daJQt9doR02lLfOVSmVWOW7e7DgA7pLAD39IgeqoTHdxmz0odHVm',
     '刘子轩',
     'CANDIDATE'),
    ('mengqi.zhang@demo.io',
     '$2b$12$8daJQt9doR02lLfOVSmVWOW7e7DgA7pLAD39IgeqoTHdxmz0odHVm',
     '张梦琪',
     'CANDIDATE');

-- ────────────────────────────────────────────────────────────
--  种子：组织树（M6）
--    - 根：「XX科技集团」（固定 id=1，业务上不可编辑）
--    - 5 个中间部门（直挂根；显式 id 便于后续 INSERT 引用，最后 setval 矫正序列）
--    - 14 个子部门：按现有 25 个 seed jobs 的 (部门, location) 组合拆分
-- ────────────────────────────────────────────────────────────

INSERT INTO root_orgs (id, name) VALUES (1, 'XX科技集团');
SELECT setval('root_orgs_id_seq', 1);

INSERT INTO departments (id, root_org_id, parent_department_id, name) VALUES
    (1, 1, NULL, '技术研发'),
    (2, 1, NULL, '产品设计'),
    (3, 1, NULL, '市场营销'),
    (4, 1, NULL, '人力资源'),
    (5, 1, NULL, '财务法务');
SELECT setval('departments_id_seq', 5);

INSERT INTO sub_departments (id, parent_department_id, name, location) VALUES
    -- 技术研发拆 5（覆盖 jobs#1..8 的 4 个地点）
    (1, 1, '技术研发-上海浦东', '上海·浦东'),
    (2, 1, '技术研发-远程办公', '远程办公'),
    (3, 1, '技术研发-北京海淀', '北京·海淀'),
    (4, 1, '技术研发-上海徐汇', '上海·徐汇'),
    (5, 1, '技术研发-杭州西湖', '杭州·西湖'),
    -- 产品设计拆 3（覆盖 jobs#9..13）
    (6, 2, '产品设计-北京朝阳', '北京·朝阳'),
    (7, 2, '产品设计-上海徐汇', '上海·徐汇'),
    (8, 2, '产品设计-上海浦东', '上海·浦东'),
    -- 市场营销拆 2（覆盖 jobs#14..17）
    (9, 3, '市场营销-上海静安', '上海·静安'),
    (10, 3, '市场营销-远程办公', '远程办公'),
    -- 人力资源拆 2（覆盖 jobs#18..21）
    (11, 4, '人力资源-上海浦东', '上海·浦东'),
    (12, 4, '人力资源-北京朝阳', '北京·朝阳'),
    -- 财务法务拆 2（覆盖 jobs#22..25）
    (13, 5, '财务法务-上海浦东', '上海·浦东'),
    (14, 5, '财务法务-北京朝阳', '北京·朝阳');
SELECT setval('sub_departments_id_seq', 14);

-- ────────────────────────────────────────────────────────────
--  种子：标签库（前端选岗位标签时的下拉选项）
-- ────────────────────────────────────────────────────────────

INSERT INTO tags (slug, name, category) VALUES
    -- 技术栈
    ('java',         'Java',         'TECH'),
    ('spring-boot',  'Spring Boot',  'TECH'),
    ('typescript',   'TypeScript',   'TECH'),
    ('vue3',         'Vue 3',        'TECH'),
    ('react',        'React',        'TECH'),
    ('node',         'Node.js',      'TECH'),
    ('python',       'Python',       'TECH'),
    ('go',           'Go',           'TECH'),
    ('postgres',     'PostgreSQL',   'TECH'),
    ('redis',        'Redis',        'TECH'),
    ('docker',       'Docker',       'TECH'),
    ('kubernetes',   'Kubernetes',   'TECH'),
    ('aws',          'AWS',          'TECH'),
    -- 软实力
    ('teamwork',     '团队协作',     'SOFT'),
    ('communication','跨部门沟通',   'SOFT'),
    ('ownership',    'Ownership',    'SOFT'),
    -- 证书
    ('pmp',          'PMP',          'CERT'),
    ('aws-saa',      'AWS SAA',      'CERT'),
    -- 语言
    ('english',      '英语流利',     'LANG'),
    ('japanese-n1',  '日语 N1',      'LANG'),
    -- 业务领域
    ('fintech',      '金融科技',     'DOMAIN'),
    ('ecommerce',    '电商',         'DOMAIN'),
    ('saas-b2b',     'SaaS B2B',     'DOMAIN'),
    ('hr-tech',      'HR Tech',      'DOMAIN');

-- ────────────────────────────────────────────────────────────
--  种子：示例岗位（25 条，覆盖 5 部门 × 5 种状态）
--    - M6 改造：sub_department_id 取代旧 department_id；location 列已 DROP
--    - 大部分由 HR (users.id=2) 创建；少量由 ADMIN (users.id=1) 创建
--    - 状态分布：18 PUBLISHED · 3 DRAFT · 2 PAUSED · 1 CLOSED · 1 ARCHIVED
--    - 子部门覆盖：14 个子部门均有岗位
--    - id 顺序与下方 INSERT 一致（id=1 高级 Java / id=2 Vue 中级 / ...）
-- ────────────────────────────────────────────────────────────

INSERT INTO jobs (
    sub_department_id, created_by, title, description,
    work_type, level, salary_min, salary_max, headcount,
    status, published_at
) VALUES
    -- ═════════ 技术研发（8 个，sub_dept 1..5） ═════════
    (1, 2,
     '高级 Java 后端工程师',
     E'## 岗位描述\n\n负责 ATS 招聘平台核心服务的设计与开发，使用 Java 21 + Spring Boot 3.4 + PostgreSQL 16。\n\n## 任职要求\n\n- 5+ 年 Java 服务端开发经验\n- 精通 Spring 生态（Boot / Security / Data）\n- 熟悉 PostgreSQL 索引与调优、Redis 缓存设计\n- 有 Docker / Kubernetes 部署经验加分',
     'FULL_TIME', 'SENIOR', 30000, 50000, 2,
     'PUBLISHED', NOW() - INTERVAL '3 days'),

    (2, 2,
     'Vue 前端工程师（中级）',
     E'## 岗位描述\n\n参与 ATS 招聘平台前端建设，技术栈 Vue 3.5 + TypeScript + Naive UI + UnoCSS。\n\n## 任职要求\n\n- 3+ 年 Vue 项目经验\n- 熟悉 TypeScript / Pinia / Vue Router\n- 注重交互细节，懂动效曲线设计',
     'REMOTE', 'MID', 20000, 35000, 1,
     'PUBLISHED', NOW() - INTERVAL '1 day'),

    (3, 2,
     '资深 Go 微服务工程师',
     E'## 岗位描述\n\n负责内部基础设施服务（消息队列网关、配置中心）的开发，技术栈 Go 1.22 + gRPC + etcd。\n\n## 任职要求\n\n- 4+ 年 Go 后端经验\n- 熟悉云原生体系（k8s operator / service mesh）\n- 有 SRE 或 infra 团队工作经验加分',
     'FULL_TIME', 'SENIOR', 35000, 55000, 1,
     'PUBLISHED', NOW() - INTERVAL '5 days'),

    (1, 2,
     'DevOps / SRE 工程师',
     E'## 岗位描述\n\n维护 K8s 集群、CI/CD 流水线、监控告警体系，推进基础设施 Code 化。\n\n## 任职要求\n\n- 3+ 年 SRE 或 DevOps 经验\n- 熟悉 Terraform / Ansible / Prometheus / Grafana\n- 写过自动化脚本，能承担线上排障',
     'FULL_TIME', 'MID', 25000, 40000, 1,
     'PUBLISHED', NOW() - INTERVAL '7 days'),

    (4, 2,
     'iOS 移动端工程师',
     E'## 岗位描述\n\n负责候选人移动端 App 开发（招聘管理 iOS 端），Swift + SwiftUI + Combine。\n\n## 任职要求\n\n- 3+ 年 iOS 经验\n- 熟悉 SwiftUI 与现代响应式架构\n- 注重性能与体验细节',
     'FULL_TIME', 'MID', 22000, 38000, 1,
     'PUBLISHED', NOW() - INTERVAL '4 days'),

    (5, 1,
     '初级算法工程师（实习转正）',
     E'## 岗位描述\n\n参与简历智能解析与岗位推荐算法迭代，方向 NLP / 推荐系统。\n\n## 任职要求\n\n- 计算机 / 数学相关硕士在读或 1 年内毕业\n- 熟悉 Python + PyTorch\n- 有 Kaggle 或 ACM 竞赛经验加分',
     'INTERN', 'INTERN', 8000, 12000, 2,
     'PUBLISHED', NOW() - INTERVAL '2 days'),

    (1, 2,
     '后端架构师（暂停收件）',
     E'## 岗位描述\n\n规划与演进招聘平台技术架构，主导跨团队技术评审。当前已收到足够简历，暂停收件。',
     'FULL_TIME', 'LEAD', 50000, 80000, 1,
     'PAUSED', NOW() - INTERVAL '20 days'),

    (1, 2,
     'QA 自动化测试工程师（草稿）',
     E'## 岗位描述（草稿）\n\nE2E 自动化测试体系搭建，技术栈待定（候选 Playwright vs Cypress）。\n\n详细 JD 撰写中。',
     'FULL_TIME', 'MID', 18000, 30000, 1,
     'DRAFT', NULL),

    -- ═════════ 产品设计（5 个，sub_dept 6..8） ═════════
    (6, 2,
     '产品经理（招聘平台方向）',
     E'## 岗位描述\n\n负责 ATS 产品规划，深耕招聘场景，与 HR 用户共建需求。\n\n## 任职要求\n\n- 3+ 年 B2B SaaS 产品经验\n- 有 HR 或人力资源行业背景优先\n- 能写清晰的 PRD 与决策文档',
     'FULL_TIME', 'SENIOR', 25000, 45000, 1,
     'PUBLISHED', NOW() - INTERVAL '6 days'),

    (7, 2,
     '高级 UI/UX 设计师',
     E'## 岗位描述\n\n负责招聘平台整体视觉与交互设计，主导设计系统建设。\n\n## 任职要求\n\n- 4+ 年 B 端 SaaS 设计经验\n- Figma 重度用户，能输出可落地的组件库\n- 关注微交互与动效',
     'FULL_TIME', 'SENIOR', 24000, 40000, 1,
     'PUBLISHED', NOW() - INTERVAL '8 days'),

    (6, 2,
     '用户研究员',
     E'## 岗位描述\n\n执行可用性测试、用户访谈、问卷调研，输出研究报告辅助产品决策。\n\n## 任职要求\n\n- 2+ 年用户研究经验\n- 心理学 / 社会学 / HCI 背景优先',
     'FULL_TIME', 'MID', 18000, 28000, 1,
     'PUBLISHED', NOW() - INTERVAL '10 days'),

    (6, 1,
     '初级产品助理(已关闭)',
     E'## 岗位描述\n\n协助产品经理梳理需求与日常事务。已招到合适人选，岗位关闭。',
     'FULL_TIME', 'JUNIOR', 12000, 18000, 1,
     'CLOSED', NOW() - INTERVAL '60 days'),

    (8, 2,
     '前端架构师(草稿)',
     E'## 岗位描述(草稿)\n\n前端基础设施与构建工具链规划，详细 JD 待补。',
     'FULL_TIME', 'LEAD', 40000, 65000, 1,
     'DRAFT', NULL),

    -- ═════════ 市场营销（4 个，sub_dept 9..10） ═════════
    (9, 2,
     '内容营销经理',
     E'## 岗位描述\n\n负责招聘平台内容矩阵建设（公众号 / 知乎 / 行业白皮书）。\n\n## 任职要求\n\n- 3+ 年 B2B 内容营销经验\n- 文笔扎实，能独立产出深度长文',
     'FULL_TIME', 'MID', 18000, 30000, 1,
     'PUBLISHED', NOW() - INTERVAL '9 days'),

    (9, 2,
     '增长 / 投放工程师',
     E'## 岗位描述\n\n执行付费投放（百度 / 腾讯广点通 / 朋友圈），分析转化漏斗。\n\n## 任职要求\n\n- 2+ 年 B 端获客 / 增长经验\n- 熟悉投放后台与归因模型',
     'FULL_TIME', 'MID', 16000, 28000, 1,
     'PUBLISHED', NOW() - INTERVAL '11 days'),

    (9, 2,
     '品牌 / 视觉设计师',
     E'## 岗位描述\n\n负责品牌物料、活动 KV、社交媒体视觉输出。\n\n## 任职要求\n\n- 3+ 年品牌设计经验\n- 平面 + 动效双能力优先',
     'CONTRACT', 'MID', 15000, 25000, 1,
     'PUBLISHED', NOW() - INTERVAL '13 days'),

    (10, 2,
     '社群运营专员(暂停)',
     E'## 岗位描述\n\n候选人社群与 HR 用户社群的日常运营。岗位暂时暂停。',
     'REMOTE', 'JUNIOR', 10000, 16000, 1,
     'PAUSED', NOW() - INTERVAL '30 days'),

    -- ═════════ 人力资源（4 个，sub_dept 11..12） ═════════
    (11, 2,
     'HR 业务伙伴（HRBP）',
     E'## 岗位描述\n\n对接技术研发部门，承接组织发展、人才盘点、绩效落地等事项。\n\n## 任职要求\n\n- 5+ 年 HRBP 或 HR 综合岗经验\n- 服务过技术团队优先',
     'FULL_TIME', 'SENIOR', 25000, 40000, 1,
     'PUBLISHED', NOW() - INTERVAL '12 days'),

    (11, 2,
     '招聘官（技术方向）',
     E'## 岗位描述\n\n承接技术研发部门招聘需求，覆盖 Java / 前端 / 算法等岗位。\n\n## 任职要求\n\n- 3+ 年技术岗招聘经验\n- 有自己的候选人池子优先',
     'FULL_TIME', 'MID', 18000, 30000, 2,
     'PUBLISHED', NOW() - INTERVAL '5 days'),

    (12, 2,
     '薪酬绩效专员',
     E'## 岗位描述\n\n负责薪酬体系日常运行（调薪 / 绩效结算 / 薪酬带宽分析）。\n\n## 任职要求\n\n- 2+ 年薪酬相关经验\n- Excel / 数据透视熟练',
     'FULL_TIME', 'JUNIOR', 12000, 20000, 1,
     'PUBLISHED', NOW() - INTERVAL '15 days'),

    (11, 1,
     'HR 实习生(已归档)',
     E'## 岗位描述\n\n协助招聘日常事务的实习岗，已暂时不开放，归档备查。',
     'INTERN', 'INTERN', 4000, 6000, 1,
     'ARCHIVED', NOW() - INTERVAL '90 days'),

    -- ═════════ 财务法务（4 个，sub_dept 13..14） ═════════
    (13, 1,
     '高级财务经理',
     E'## 岗位描述\n\n负责公司财务体系建设，对接审计与税务事项。\n\n## 任职要求\n\n- 8+ 年财务经验，3+ 年管理\n- CPA / ACCA 优先',
     'FULL_TIME', 'SENIOR', 30000, 50000, 1,
     'PUBLISHED', NOW() - INTERVAL '7 days'),

    (13, 2,
     '税务专员',
     E'## 岗位描述\n\n日常增值税 / 企税申报、税务筹划支持。\n\n## 任职要求\n\n- 2+ 年税务相关工作经验\n- CTA 在读优先',
     'FULL_TIME', 'JUNIOR', 12000, 20000, 1,
     'PUBLISHED', NOW() - INTERVAL '14 days'),

    (14, 1,
     '法务顾问',
     E'## 岗位描述\n\n商业合同审核、知识产权维护、合规咨询。\n\n## 任职要求\n\n- 法学硕士 + 3 年企业法务\n- 通过国家司法考试',
     'FULL_TIME', 'MID', 25000, 40000, 1,
     'PUBLISHED', NOW() - INTERVAL '6 days'),

    (13, 2,
     '内审主管(草稿)',
     E'## 岗位描述(草稿)\n\n内部审计体系搭建，详细 JD 待补。',
     'FULL_TIME', 'SENIOR', 28000, 45000, 1,
     'DRAFT', NULL);

-- ────────────────────────────────────────────────────────────
--  HR 用户 ↔ 子部门 多对多绑定（M6）
--    - hr@ats.local (id=2) 是种子兼容性 HR，名下挂 25 个跨部门岗位
--    - 给他绑定全部 14 个子部门，确保 HR 视角 + owner 鉴权链路不破
--    - 业务后续创建新 HR 时由 admin 在 /admin/users 表单按需选子部门
-- ────────────────────────────────────────────────────────────

INSERT INTO hr_sub_departments (user_id, sub_department_id)
SELECT 2, id FROM sub_departments;

-- 关联标签 · 仅给 PUBLISHED 与少量 PAUSED 岗位补，DRAFT/CLOSED/ARCHIVED 不挂标签
INSERT INTO job_tags (job_id, tag_id)
SELECT 1, id FROM tags WHERE slug IN ('java', 'spring-boot', 'postgres', 'redis', 'docker', 'kubernetes', 'ownership');
INSERT INTO job_tags (job_id, tag_id)
SELECT 2, id FROM tags WHERE slug IN ('vue3', 'typescript', 'teamwork', 'communication');
INSERT INTO job_tags (job_id, tag_id)
SELECT 3, id FROM tags WHERE slug IN ('go', 'kubernetes', 'docker', 'ownership');
INSERT INTO job_tags (job_id, tag_id)
SELECT 4, id FROM tags WHERE slug IN ('docker', 'kubernetes', 'aws', 'ownership');
INSERT INTO job_tags (job_id, tag_id)
SELECT 5, id FROM tags WHERE slug IN ('teamwork', 'communication');
INSERT INTO job_tags (job_id, tag_id)
SELECT 6, id FROM tags WHERE slug IN ('python', 'teamwork');
INSERT INTO job_tags (job_id, tag_id)
SELECT 9, id FROM tags WHERE slug IN ('saas-b2b', 'hr-tech', 'communication', 'english');
INSERT INTO job_tags (job_id, tag_id)
SELECT 10, id FROM tags WHERE slug IN ('saas-b2b', 'communication');
INSERT INTO job_tags (job_id, tag_id)
SELECT 11, id FROM tags WHERE slug IN ('saas-b2b', 'communication');
INSERT INTO job_tags (job_id, tag_id)
SELECT 14, id FROM tags WHERE slug IN ('saas-b2b', 'communication', 'english');
INSERT INTO job_tags (job_id, tag_id)
SELECT 15, id FROM tags WHERE slug IN ('teamwork', 'communication');
INSERT INTO job_tags (job_id, tag_id)
SELECT 16, id FROM tags WHERE slug IN ('communication', 'teamwork');
INSERT INTO job_tags (job_id, tag_id)
SELECT 18, id FROM tags WHERE slug IN ('hr-tech', 'communication', 'english');
INSERT INTO job_tags (job_id, tag_id)
SELECT 19, id FROM tags WHERE slug IN ('hr-tech', 'communication', 'teamwork');
INSERT INTO job_tags (job_id, tag_id)
SELECT 20, id FROM tags WHERE slug IN ('hr-tech', 'teamwork');
INSERT INTO job_tags (job_id, tag_id)
SELECT 22, id FROM tags WHERE slug IN ('fintech', 'english', 'communication');
INSERT INTO job_tags (job_id, tag_id)
SELECT 23, id FROM tags WHERE slug IN ('fintech', 'communication');
INSERT INTO job_tags (job_id, tag_id)
SELECT 24, id FROM tags WHERE slug IN ('fintech', 'english', 'communication');

-- ────────────────────────────────────────────────────────────
--  种子：示例投递（候选人 → 高级 Java 后端工程师）
--    - candidate_id = 3 (李哲) → job_id = 1
--    - stage = APPLIED（刚投递，未筛选）
--    - 配套写入一条 stage_logs（业务规则：投递时 from_stage=NULL → to_stage=APPLIED）
-- ────────────────────────────────────────────────────────────

INSERT INTO applications (
    job_id, candidate_id, stage, resume_url, years_exp, phone, applied_at
) VALUES
    (1, 3, 'APPLIED', NULL, 5, '13900001111', NOW() - INTERVAL '2 hours');

INSERT INTO stage_logs (
    application_id, operated_by, from_stage, to_stage, note, operated_at
) VALUES
    (1, 3, NULL, 'APPLIED', '候选人主动投递（seed 数据）', NOW() - INTERVAL '2 hours');

-- ────────────────────────────────────────────────────────────
--  种子：10 条多 stage 示例投递，让看板各阶段都有数据
--    - 5 名候选人 × 不同岗位组合，覆盖 8 态状态机里的 7 个（HIRED 留空，避免演示时太静态）
--    - 每条都配套完整 stage_logs 链（业务约束：从 APPLIED 开始顺序流转，REJECTED 必填 reason）
--    - operated_by：投递（NULL→APPLIED）= 候选人自己；流转 = HR (id=2)
--    - updated_at：手动设为「最后一次流转时间」，让看板按 updated_at DESC 排序时分布合理
--      （INSERT 不触发 trg_applications_updated_at，trigger 仅 BEFORE UPDATE，所以可直接指定）
-- ────────────────────────────────────────────────────────────

DO $seed_apps$
DECLARE
    c_wang   bigint := (SELECT id FROM users WHERE email = 'xiaotong.wang@demo.io');
    c_chen   bigint := (SELECT id FROM users WHERE email = 'haochen@demo.io');
    c_lin    bigint := (SELECT id FROM users WHERE email = 'qingya.lin@demo.io');
    c_liu    bigint := (SELECT id FROM users WHERE email = 'zixuan.liu@demo.io');
    c_zhang  bigint := (SELECT id FROM users WHERE email = 'mengqi.zhang@demo.io');
    hr_id    bigint := (SELECT id FROM users WHERE email = 'hr@ats.local');
    aid      bigint;
BEGIN
    -- #1 王晓彤 → Vue 前端（job_id=2）→ APPLIED ──────────────────────
    INSERT INTO applications (job_id, candidate_id, stage, years_exp, phone, applied_at, updated_at)
    VALUES (2, c_wang, 'APPLIED', 6, '13900002001', NOW() - INTERVAL '1 day', NOW() - INTERVAL '1 day')
    RETURNING id INTO aid;
    INSERT INTO stage_logs (application_id, operated_by, from_stage, to_stage, note, operated_at)
    VALUES (aid, c_wang, NULL, 'APPLIED', '候选人主动投递', NOW() - INTERVAL '1 day');

    -- #2 陈昊 → 高级 Java（job_id=1）→ SCREENING_PASS ────────────────
    INSERT INTO applications (job_id, candidate_id, stage, years_exp, phone, applied_at, updated_at)
    VALUES (1, c_chen, 'SCREENING_PASS', 5, '13900002002', NOW() - INTERVAL '3 days', NOW() - INTERVAL '2 days')
    RETURNING id INTO aid;
    INSERT INTO stage_logs (application_id, operated_by, from_stage, to_stage, note, operated_at) VALUES
        (aid, c_chen, NULL, 'APPLIED', '候选人主动投递', NOW() - INTERVAL '3 days'),
        (aid, hr_id, 'APPLIED', 'SCREENING_PASS', '简历亮点：Spring + PG 调优经验', NOW() - INTERVAL '2 days');

    -- #3 林清雅 → UI/UX（job_id=10）→ PHONE_INTERVIEW ────────────────
    INSERT INTO applications (job_id, candidate_id, stage, years_exp, phone, applied_at, updated_at)
    VALUES (10, c_lin, 'PHONE_INTERVIEW', 4, '13900002003', NOW() - INTERVAL '5 days', NOW() - INTERVAL '3 days')
    RETURNING id INTO aid;
    INSERT INTO stage_logs (application_id, operated_by, from_stage, to_stage, note, operated_at) VALUES
        (aid, c_lin, NULL, 'APPLIED', '候选人主动投递', NOW() - INTERVAL '5 days'),
        (aid, hr_id, 'APPLIED', 'SCREENING_PASS', '作品集质量高，B 端项目经验匹配', NOW() - INTERVAL '4 days'),
        (aid, hr_id, 'SCREENING_PASS', 'PHONE_INTERVIEW', '约电话初面 · 周三 14:00', NOW() - INTERVAL '3 days');

    -- #4 刘子轩 → Go 微服务（job_id=3）→ TECH_INTERVIEW ──────────────
    INSERT INTO applications (job_id, candidate_id, stage, years_exp, phone, applied_at, updated_at)
    VALUES (3, c_liu, 'TECH_INTERVIEW', 7, '13900002004', NOW() - INTERVAL '7 days', NOW() - INTERVAL '3 days')
    RETURNING id INTO aid;
    INSERT INTO stage_logs (application_id, operated_by, from_stage, to_stage, note, operated_at) VALUES
        (aid, c_liu, NULL, 'APPLIED', '候选人主动投递', NOW() - INTERVAL '7 days'),
        (aid, hr_id, 'APPLIED', 'SCREENING_PASS', 'k8s operator 经验对口', NOW() - INTERVAL '6 days'),
        (aid, hr_id, 'SCREENING_PASS', 'PHONE_INTERVIEW', '电话初面通过，沟通顺畅', NOW() - INTERVAL '5 days'),
        (aid, hr_id, 'PHONE_INTERVIEW', 'TECH_INTERVIEW', '安排技术二面（部门 leader）', NOW() - INTERVAL '3 days');

    -- #5 张梦琪 → 产品经理（job_id=9）→ HR_INTERVIEW ────────────────
    INSERT INTO applications (job_id, candidate_id, stage, years_exp, phone, applied_at, updated_at)
    VALUES (9, c_zhang, 'HR_INTERVIEW', 4, '13900002005', NOW() - INTERVAL '6 days', NOW() - INTERVAL '1 day')
    RETURNING id INTO aid;
    INSERT INTO stage_logs (application_id, operated_by, from_stage, to_stage, note, operated_at) VALUES
        (aid, c_zhang, NULL, 'APPLIED', '候选人主动投递', NOW() - INTERVAL '6 days'),
        (aid, hr_id, 'APPLIED', 'SCREENING_PASS', 'HR Tech 行业背景匹配', NOW() - INTERVAL '5 days'),
        (aid, hr_id, 'SCREENING_PASS', 'PHONE_INTERVIEW', '电话初面通过', NOW() - INTERVAL '4 days'),
        (aid, hr_id, 'PHONE_INTERVIEW', 'TECH_INTERVIEW', '产品评审顺利', NOW() - INTERVAL '3 days'),
        (aid, hr_id, 'TECH_INTERVIEW', 'HR_INTERVIEW', '约 HR 终面 · 周五', NOW() - INTERVAL '1 day');

    -- #6 王晓彤 → DevOps（job_id=4）→ OFFER ─────────────────────────
    INSERT INTO applications (job_id, candidate_id, stage, years_exp, phone, applied_at, updated_at)
    VALUES (4, c_wang, 'OFFER', 6, '13900002001', NOW() - INTERVAL '14 days', NOW() - INTERVAL '1 day')
    RETURNING id INTO aid;
    INSERT INTO stage_logs (application_id, operated_by, from_stage, to_stage, note, operated_at) VALUES
        (aid, c_wang, NULL, 'APPLIED', '候选人主动投递', NOW() - INTERVAL '14 days'),
        (aid, hr_id, 'APPLIED', 'SCREENING_PASS', 'Terraform / Prometheus 经验加分', NOW() - INTERVAL '12 days'),
        (aid, hr_id, 'SCREENING_PASS', 'PHONE_INTERVIEW', '电话初面通过', NOW() - INTERVAL '10 days'),
        (aid, hr_id, 'PHONE_INTERVIEW', 'TECH_INTERVIEW', '技术二面通过', NOW() - INTERVAL '7 days'),
        (aid, hr_id, 'TECH_INTERVIEW', 'HR_INTERVIEW', 'HR 终面通过', NOW() - INTERVAL '4 days'),
        (aid, hr_id, 'HR_INTERVIEW', 'OFFER', '已发 Offer，待候选人回复', NOW() - INTERVAL '1 day');

    -- #7 陈昊 → 招聘官（job_id=19，跨方向投递）→ APPLIED ─────────────
    INSERT INTO applications (job_id, candidate_id, stage, years_exp, phone, applied_at, updated_at)
    VALUES (19, c_chen, 'APPLIED', 5, '13900002002', NOW() - INTERVAL '12 hours', NOW() - INTERVAL '12 hours')
    RETURNING id INTO aid;
    INSERT INTO stage_logs (application_id, operated_by, from_stage, to_stage, note, operated_at)
    VALUES (aid, c_chen, NULL, 'APPLIED', '候选人主动投递（同时投了 Java 岗）', NOW() - INTERVAL '12 hours');

    -- #8 林清雅 → 品牌设计（job_id=16）→ REJECTED ───────────────────
    INSERT INTO applications (job_id, candidate_id, stage, resume_url, years_exp, phone, reject_reason, applied_at, updated_at)
    VALUES (16, c_lin, 'REJECTED', NULL, 4, '13900002003', '美术风格与品牌调性差异较大',
            NOW() - INTERVAL '4 days', NOW() - INTERVAL '2 days')
    RETURNING id INTO aid;
    INSERT INTO stage_logs (application_id, operated_by, from_stage, to_stage, note, operated_at) VALUES
        (aid, c_lin, NULL, 'APPLIED', '候选人主动投递', NOW() - INTERVAL '4 days'),
        (aid, hr_id, 'APPLIED', 'REJECTED', '美术风格与品牌调性差异较大', NOW() - INTERVAL '2 days');

    -- #9 刘子轩 → iOS（job_id=5）→ APPLIED ──────────────────────────
    INSERT INTO applications (job_id, candidate_id, stage, years_exp, phone, applied_at, updated_at)
    VALUES (5, c_liu, 'APPLIED', 7, '13900002004', NOW() - INTERVAL '2 hours', NOW() - INTERVAL '2 hours')
    RETURNING id INTO aid;
    INSERT INTO stage_logs (application_id, operated_by, from_stage, to_stage, note, operated_at)
    VALUES (aid, c_liu, NULL, 'APPLIED', '候选人主动投递（同时也投了 Go 岗）', NOW() - INTERVAL '2 hours');

    -- #10 张梦琪 → 内容营销（job_id=14）→ SCREENING_PASS ────────────
    INSERT INTO applications (job_id, candidate_id, stage, years_exp, phone, applied_at, updated_at)
    VALUES (14, c_zhang, 'SCREENING_PASS', 4, '13900002005', NOW() - INTERVAL '2 days', NOW() - INTERVAL '1 day')
    RETURNING id INTO aid;
    INSERT INTO stage_logs (application_id, operated_by, from_stage, to_stage, note, operated_at) VALUES
        (aid, c_zhang, NULL, 'APPLIED', '候选人主动投递（产品方向之外的试探）', NOW() - INTERVAL '2 days'),
        (aid, hr_id, 'APPLIED', 'SCREENING_PASS', '文笔扎实，公众号案例有数据', NOW() - INTERVAL '1 day');
END $seed_apps$;

-- ────────────────────────────────────────────────────────────
--  视图：招聘漏斗（各阶段申请数，用于数据看板）
-- ────────────────────────────────────────────────────────────

CREATE OR REPLACE VIEW v_pipeline_funnel AS
SELECT
    j.id          AS job_id,
    j.title       AS job_title,
    a.stage,
    COUNT(*)      AS cnt
FROM applications a
JOIN jobs j ON j.id = a.job_id
WHERE j.deleted_at IS NULL
GROUP BY j.id, j.title, a.stage
ORDER BY j.id, a.stage;

COMMENT ON VIEW v_pipeline_funnel IS '按岗位和阶段聚合的申请数量，供招聘漏斗图使用';

-- ────────────────────────────────────────────────────────────
--  视图：本月招聘概览（数据看板首页卡片）
-- ────────────────────────────────────────────────────────────

CREATE OR REPLACE VIEW v_monthly_overview AS
SELECT
    COUNT(*) FILTER (WHERE applied_at >= date_trunc('month', NOW()))  AS new_applications,
    COUNT(*) FILTER (WHERE stage = 'OFFER'
                      AND updated_at >= date_trunc('month', NOW()))   AS offers_sent,
    COUNT(*) FILTER (WHERE stage = 'HIRED'
                      AND updated_at >= date_trunc('month', NOW()))   AS hires
FROM applications;

COMMENT ON VIEW v_monthly_overview IS '本月新增申请数、Offer 数、入职数，供首页概览卡片使用';
