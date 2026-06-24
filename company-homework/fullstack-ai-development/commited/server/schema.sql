--
-- PostgreSQL database dump
--

\restrict lyYBUXWsHydir5vqS6jmc8eXDvOBsi5H8i0dU3NgiP5QFSye27dRfmYzGrPup9i

-- Dumped from database version 16.14 (Debian 16.14-1.pgdg13+1)
-- Dumped by pg_dump version 16.14 (Debian 16.14-1.pgdg13+1)

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: approval_instances; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.approval_instances (
    id bigint NOT NULL,
    biz_type character varying(32) NOT NULL,
    biz_id bigint NOT NULL,
    status character varying(16) DEFAULT 'pending'::character varying NOT NULL,
    current_step integer DEFAULT 1 NOT NULL,
    total_steps integer DEFAULT 1 NOT NULL,
    submitted_by bigint DEFAULT 0 NOT NULL,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_at timestamp with time zone DEFAULT now() NOT NULL
);


--
-- Name: approval_steps; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.approval_steps (
    id bigint NOT NULL,
    instance_id bigint NOT NULL,
    step_no integer NOT NULL,
    approver_id bigint NOT NULL,
    action character varying(16) DEFAULT 'pending'::character varying NOT NULL,
    comment text DEFAULT ''::text NOT NULL,
    acted_at timestamp with time zone,
    created_at timestamp with time zone DEFAULT now() NOT NULL
);


--
-- Name: audit_logs; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.audit_logs (
    id bigint NOT NULL,
    user_id bigint DEFAULT 0 NOT NULL,
    username character varying(64) DEFAULT ''::character varying NOT NULL,
    method character varying(16) DEFAULT ''::character varying NOT NULL,
    path character varying(256) DEFAULT ''::character varying NOT NULL,
    status integer DEFAULT 0 NOT NULL,
    ip character varying(64) DEFAULT ''::character varying NOT NULL,
    request_id character varying(64) DEFAULT ''::character varying NOT NULL,
    body text DEFAULT ''::text NOT NULL,
    created_at timestamp with time zone DEFAULT now() NOT NULL
);


--
-- Name: audit_logs_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.audit_logs_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: audit_logs_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.audit_logs_id_seq OWNED BY public.audit_logs.id;


--
-- Name: contract_templates; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.contract_templates (
    id bigint NOT NULL,
    name character varying(128) NOT NULL,
    type character varying(32) DEFAULT 'fixed_term'::character varying NOT NULL,
    content text DEFAULT ''::text NOT NULL,
    enabled boolean DEFAULT true NOT NULL,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_at timestamp with time zone DEFAULT now() NOT NULL
);


--
-- Name: contracts; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.contracts (
    id bigint NOT NULL,
    contract_no character varying(64) NOT NULL,
    employee_id bigint NOT NULL,
    template_id bigint,
    type character varying(32) DEFAULT 'fixed_term'::character varying NOT NULL,
    status character varying(16) DEFAULT 'active'::character varying NOT NULL,
    sign_date date,
    start_date date,
    end_date date,
    prev_contract_id bigint,
    salary_band character varying(64) DEFAULT ''::character varying NOT NULL,
    terms jsonb DEFAULT '{}'::jsonb NOT NULL,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_at timestamp with time zone DEFAULT now() NOT NULL,
    deleted_at timestamp with time zone
);


--
-- Name: data_dicts; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.data_dicts (
    id bigint NOT NULL,
    category character varying(64) NOT NULL,
    code character varying(64) NOT NULL,
    label character varying(128) NOT NULL,
    sort_order integer DEFAULT 0 NOT NULL,
    enabled boolean DEFAULT true NOT NULL,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_at timestamp with time zone DEFAULT now() NOT NULL
);


--
-- Name: departments; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.departments (
    id bigint NOT NULL,
    org_id bigint NOT NULL,
    parent_id bigint,
    code character varying(64) NOT NULL,
    name character varying(128) NOT NULL,
    short_name character varying(64) DEFAULT ''::character varying NOT NULL,
    path character varying(512) NOT NULL,
    sort_order integer DEFAULT 0 NOT NULL,
    head_user_id bigint,
    status character varying(16) DEFAULT 'active'::character varying NOT NULL,
    description text DEFAULT ''::text NOT NULL,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_at timestamp with time zone DEFAULT now() NOT NULL,
    deleted_at timestamp with time zone
);


--
-- Name: employee_job_history; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.employee_job_history (
    id bigint NOT NULL,
    employee_id bigint NOT NULL,
    change_type character varying(32) NOT NULL,
    from_snapshot jsonb DEFAULT '{}'::jsonb NOT NULL,
    to_snapshot jsonb DEFAULT '{}'::jsonb NOT NULL,
    process_id bigint,
    effective_date date,
    remark text DEFAULT ''::text NOT NULL,
    created_at timestamp with time zone DEFAULT now() NOT NULL
);


--
-- Name: employees; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.employees (
    id bigint NOT NULL,
    employee_no character varying(64) NOT NULL,
    user_id bigint,
    name character varying(64) NOT NULL,
    en_name character varying(64) DEFAULT ''::character varying NOT NULL,
    gender character varying(16) DEFAULT ''::character varying NOT NULL,
    birthday date,
    avatar character varying(256) DEFAULT ''::character varying NOT NULL,
    phone character varying(32) DEFAULT ''::character varying NOT NULL,
    work_email character varying(128) DEFAULT ''::character varying NOT NULL,
    education character varying(32) DEFAULT ''::character varying NOT NULL,
    org_id bigint DEFAULT 0 NOT NULL,
    org_path character varying(512) DEFAULT ''::character varying NOT NULL,
    dept_id bigint,
    dept_name character varying(128) DEFAULT ''::character varying NOT NULL,
    position_id bigint,
    position_name character varying(128) DEFAULT ''::character varying NOT NULL,
    job_level character varying(16) DEFAULT ''::character varying NOT NULL,
    manager_id bigint,
    employment_type character varying(32) DEFAULT 'full_time'::character varying NOT NULL,
    employment_status character varying(32) DEFAULT 'active'::character varying NOT NULL,
    hired_at date,
    regular_at date,
    left_at date,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_at timestamp with time zone DEFAULT now() NOT NULL,
    deleted_at timestamp with time zone
);


--
-- Name: hr_events; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.hr_events (
    id bigint NOT NULL,
    employee_id bigint,
    event_type character varying(32) NOT NULL,
    title character varying(256) NOT NULL,
    detail jsonb DEFAULT '{}'::jsonb NOT NULL,
    org_id bigint DEFAULT 0 NOT NULL,
    org_path character varying(512) DEFAULT ''::character varying NOT NULL,
    actor_id bigint,
    occurred_at timestamp with time zone DEFAULT now() NOT NULL
);


--
-- Name: hr_processes; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.hr_processes (
    id bigint NOT NULL,
    process_no character varying(64) NOT NULL,
    type character varying(32) NOT NULL,
    employee_id bigint,
    applicant_id bigint DEFAULT 0 NOT NULL,
    org_id bigint DEFAULT 0 NOT NULL,
    org_path character varying(512) DEFAULT ''::character varying NOT NULL,
    payload jsonb DEFAULT '{}'::jsonb NOT NULL,
    status character varying(16) DEFAULT 'pending'::character varying NOT NULL,
    effective_date date,
    result jsonb DEFAULT '{}'::jsonb NOT NULL,
    approval_id bigint,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_at timestamp with time zone DEFAULT now() NOT NULL
);


--
-- Name: job_levels; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.job_levels (
    id bigint NOT NULL,
    seq_code character varying(16) NOT NULL,
    name character varying(64) NOT NULL,
    level_code character varying(16) NOT NULL,
    level_order integer DEFAULT 0 NOT NULL,
    description text DEFAULT ''::text NOT NULL,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_at timestamp with time zone DEFAULT now() NOT NULL
);


--
-- Name: organizations; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.organizations (
    id bigint NOT NULL,
    parent_id bigint,
    code character varying(64) NOT NULL,
    name character varying(128) NOT NULL,
    short_name character varying(64) DEFAULT ''::character varying NOT NULL,
    type character varying(32) DEFAULT 'group'::character varying NOT NULL,
    path character varying(512) NOT NULL,
    sort_order integer DEFAULT 0 NOT NULL,
    status character varying(16) DEFAULT 'active'::character varying NOT NULL,
    description text DEFAULT ''::text NOT NULL,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_at timestamp with time zone DEFAULT now() NOT NULL,
    deleted_at timestamp with time zone
);


--
-- Name: positions; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.positions (
    id bigint NOT NULL,
    org_id bigint NOT NULL,
    dept_id bigint,
    code character varying(64) NOT NULL,
    name character varying(128) NOT NULL,
    job_family character varying(64) DEFAULT ''::character varying NOT NULL,
    level_seq character varying(16) DEFAULT ''::character varying NOT NULL,
    headcount integer DEFAULT 0 NOT NULL,
    status character varying(16) DEFAULT 'active'::character varying NOT NULL,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_at timestamp with time zone DEFAULT now() NOT NULL,
    deleted_at timestamp with time zone
);


--
-- Name: roles; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.roles (
    id bigint NOT NULL,
    code character varying(64) NOT NULL,
    name character varying(128) NOT NULL,
    description text DEFAULT ''::text NOT NULL,
    is_builtin boolean DEFAULT true NOT NULL,
    perms jsonb DEFAULT '[]'::jsonb NOT NULL,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_at timestamp with time zone DEFAULT now() NOT NULL
);


--
-- Name: schema_migrations; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.schema_migrations (
    version character varying(128) NOT NULL,
    applied_at timestamp with time zone DEFAULT now() NOT NULL
);


--
-- Name: users; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.users (
    id bigint NOT NULL,
    username character varying(64) NOT NULL,
    name character varying(64) NOT NULL,
    password_hash character varying(256) NOT NULL,
    employee_id bigint,
    email character varying(128) DEFAULT ''::character varying NOT NULL,
    phone character varying(32) DEFAULT ''::character varying NOT NULL,
    avatar character varying(256) DEFAULT ''::character varying NOT NULL,
    org_id bigint DEFAULT 0 NOT NULL,
    org_path character varying(512) DEFAULT ''::character varying NOT NULL,
    roles jsonb DEFAULT '[]'::jsonb NOT NULL,
    is_admin boolean DEFAULT false NOT NULL,
    is_active boolean DEFAULT true NOT NULL,
    last_login_at timestamp with time zone,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_at timestamp with time zone DEFAULT now() NOT NULL,
    deleted_at timestamp with time zone
);


--
-- Name: audit_logs id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.audit_logs ALTER COLUMN id SET DEFAULT nextval('public.audit_logs_id_seq'::regclass);


--
-- Name: approval_instances approval_instances_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.approval_instances
    ADD CONSTRAINT approval_instances_pkey PRIMARY KEY (id);


--
-- Name: approval_steps approval_steps_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.approval_steps
    ADD CONSTRAINT approval_steps_pkey PRIMARY KEY (id);


--
-- Name: audit_logs audit_logs_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.audit_logs
    ADD CONSTRAINT audit_logs_pkey PRIMARY KEY (id);


--
-- Name: contract_templates contract_templates_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.contract_templates
    ADD CONSTRAINT contract_templates_pkey PRIMARY KEY (id);


--
-- Name: contracts contracts_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.contracts
    ADD CONSTRAINT contracts_pkey PRIMARY KEY (id);


--
-- Name: data_dicts data_dicts_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.data_dicts
    ADD CONSTRAINT data_dicts_pkey PRIMARY KEY (id);


--
-- Name: departments departments_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.departments
    ADD CONSTRAINT departments_pkey PRIMARY KEY (id);


--
-- Name: employee_job_history employee_job_history_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.employee_job_history
    ADD CONSTRAINT employee_job_history_pkey PRIMARY KEY (id);


--
-- Name: employees employees_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.employees
    ADD CONSTRAINT employees_pkey PRIMARY KEY (id);


--
-- Name: hr_events hr_events_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.hr_events
    ADD CONSTRAINT hr_events_pkey PRIMARY KEY (id);


--
-- Name: hr_processes hr_processes_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.hr_processes
    ADD CONSTRAINT hr_processes_pkey PRIMARY KEY (id);


--
-- Name: job_levels job_levels_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.job_levels
    ADD CONSTRAINT job_levels_pkey PRIMARY KEY (id);


--
-- Name: organizations organizations_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.organizations
    ADD CONSTRAINT organizations_pkey PRIMARY KEY (id);


--
-- Name: positions positions_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.positions
    ADD CONSTRAINT positions_pkey PRIMARY KEY (id);


--
-- Name: roles roles_code_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.roles
    ADD CONSTRAINT roles_code_key UNIQUE (code);


--
-- Name: roles roles_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.roles
    ADD CONSTRAINT roles_pkey PRIMARY KEY (id);


--
-- Name: schema_migrations schema_migrations_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.schema_migrations
    ADD CONSTRAINT schema_migrations_pkey PRIMARY KEY (version);


--
-- Name: users users_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_pkey PRIMARY KEY (id);


--
-- Name: idx_appr_inst_biz; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_appr_inst_biz ON public.approval_instances USING btree (biz_type, biz_id);


--
-- Name: idx_appr_inst_status; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_appr_inst_status ON public.approval_instances USING btree (status);


--
-- Name: idx_appr_steps_approver; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_appr_steps_approver ON public.approval_steps USING btree (approver_id, action);


--
-- Name: idx_appr_steps_inst; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_appr_steps_inst ON public.approval_steps USING btree (instance_id);


--
-- Name: idx_audit_logs_created; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_audit_logs_created ON public.audit_logs USING btree (created_at);


--
-- Name: idx_audit_logs_path; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_audit_logs_path ON public.audit_logs USING btree (path);


--
-- Name: idx_audit_logs_user; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_audit_logs_user ON public.audit_logs USING btree (user_id);


--
-- Name: idx_contracts_emp; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_contracts_emp ON public.contracts USING btree (employee_id);


--
-- Name: idx_contracts_end; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_contracts_end ON public.contracts USING btree (end_date);


--
-- Name: idx_contracts_status; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_contracts_status ON public.contracts USING btree (status);


--
-- Name: idx_departments_org; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_departments_org ON public.departments USING btree (org_id);


--
-- Name: idx_departments_parent; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_departments_parent ON public.departments USING btree (parent_id);


--
-- Name: idx_departments_path; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_departments_path ON public.departments USING btree (path);


--
-- Name: idx_employees_dept; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_employees_dept ON public.employees USING btree (dept_id);


--
-- Name: idx_employees_name; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_employees_name ON public.employees USING btree (name);


--
-- Name: idx_employees_org_path; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_employees_org_path ON public.employees USING btree (org_path);


--
-- Name: idx_employees_status; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_employees_status ON public.employees USING btree (employment_status);


--
-- Name: idx_hr_events_emp; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_hr_events_emp ON public.hr_events USING btree (employee_id);


--
-- Name: idx_hr_events_occurred; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_hr_events_occurred ON public.hr_events USING btree (occurred_at);


--
-- Name: idx_hr_events_type; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_hr_events_type ON public.hr_events USING btree (event_type);


--
-- Name: idx_hr_processes_applicant; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_hr_processes_applicant ON public.hr_processes USING btree (applicant_id);


--
-- Name: idx_hr_processes_emp; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_hr_processes_emp ON public.hr_processes USING btree (employee_id);


--
-- Name: idx_hr_processes_status; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_hr_processes_status ON public.hr_processes USING btree (status);


--
-- Name: idx_hr_processes_type; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_hr_processes_type ON public.hr_processes USING btree (type);


--
-- Name: idx_job_history_emp; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_job_history_emp ON public.employee_job_history USING btree (employee_id);


--
-- Name: idx_organizations_parent; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_organizations_parent ON public.organizations USING btree (parent_id);


--
-- Name: idx_organizations_path; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_organizations_path ON public.organizations USING btree (path);


--
-- Name: idx_positions_dept; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_positions_dept ON public.positions USING btree (dept_id);


--
-- Name: idx_users_org_path; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_users_org_path ON public.users USING btree (org_path);


--
-- Name: idx_users_roles; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_users_roles ON public.users USING gin (roles);


--
-- Name: uq_contracts_no; Type: INDEX; Schema: public; Owner: -
--

CREATE UNIQUE INDEX uq_contracts_no ON public.contracts USING btree (contract_no) WHERE (deleted_at IS NULL);


--
-- Name: uq_data_dicts_cat_code; Type: INDEX; Schema: public; Owner: -
--

CREATE UNIQUE INDEX uq_data_dicts_cat_code ON public.data_dicts USING btree (category, code);


--
-- Name: uq_departments_org_code; Type: INDEX; Schema: public; Owner: -
--

CREATE UNIQUE INDEX uq_departments_org_code ON public.departments USING btree (org_id, code) WHERE (deleted_at IS NULL);


--
-- Name: uq_employees_no; Type: INDEX; Schema: public; Owner: -
--

CREATE UNIQUE INDEX uq_employees_no ON public.employees USING btree (employee_no) WHERE (deleted_at IS NULL);


--
-- Name: uq_hr_processes_no; Type: INDEX; Schema: public; Owner: -
--

CREATE UNIQUE INDEX uq_hr_processes_no ON public.hr_processes USING btree (process_no);


--
-- Name: uq_job_levels_seq_code; Type: INDEX; Schema: public; Owner: -
--

CREATE UNIQUE INDEX uq_job_levels_seq_code ON public.job_levels USING btree (seq_code, level_code);


--
-- Name: uq_organizations_code; Type: INDEX; Schema: public; Owner: -
--

CREATE UNIQUE INDEX uq_organizations_code ON public.organizations USING btree (code) WHERE (deleted_at IS NULL);


--
-- Name: uq_positions_dept_code; Type: INDEX; Schema: public; Owner: -
--

CREATE UNIQUE INDEX uq_positions_dept_code ON public.positions USING btree (dept_id, code) WHERE (deleted_at IS NULL);


--
-- Name: uq_users_username; Type: INDEX; Schema: public; Owner: -
--

CREATE UNIQUE INDEX uq_users_username ON public.users USING btree (username) WHERE (deleted_at IS NULL);


--
-- Name: approval_steps approval_steps_instance_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.approval_steps
    ADD CONSTRAINT approval_steps_instance_id_fkey FOREIGN KEY (instance_id) REFERENCES public.approval_instances(id) ON DELETE CASCADE;


--
-- Name: contracts contracts_employee_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.contracts
    ADD CONSTRAINT contracts_employee_id_fkey FOREIGN KEY (employee_id) REFERENCES public.employees(id) ON DELETE CASCADE;


--
-- Name: departments departments_org_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.departments
    ADD CONSTRAINT departments_org_id_fkey FOREIGN KEY (org_id) REFERENCES public.organizations(id) ON DELETE CASCADE;


--
-- Name: departments departments_parent_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.departments
    ADD CONSTRAINT departments_parent_id_fkey FOREIGN KEY (parent_id) REFERENCES public.departments(id) ON DELETE SET NULL;


--
-- Name: employee_job_history employee_job_history_employee_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.employee_job_history
    ADD CONSTRAINT employee_job_history_employee_id_fkey FOREIGN KEY (employee_id) REFERENCES public.employees(id) ON DELETE CASCADE;


--
-- Name: organizations organizations_parent_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.organizations
    ADD CONSTRAINT organizations_parent_id_fkey FOREIGN KEY (parent_id) REFERENCES public.organizations(id) ON DELETE SET NULL;


--
-- Name: positions positions_dept_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.positions
    ADD CONSTRAINT positions_dept_id_fkey FOREIGN KEY (dept_id) REFERENCES public.departments(id) ON DELETE SET NULL;


--
-- Name: positions positions_org_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.positions
    ADD CONSTRAINT positions_org_id_fkey FOREIGN KEY (org_id) REFERENCES public.organizations(id) ON DELETE CASCADE;


--
-- PostgreSQL database dump complete
--

\unrestrict lyYBUXWsHydir5vqS6jmc8eXDvOBsi5H8i0dU3NgiP5QFSye27dRfmYzGrPup9i

