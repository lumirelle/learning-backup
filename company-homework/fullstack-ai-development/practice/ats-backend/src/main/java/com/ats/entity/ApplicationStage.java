package com.ats.entity;

/**
 * 候选人申请阶段，对应 PostgreSQL application_stage type。
 * <p>
 * 8 态状态机由 {@link com.ats.application.ApplicationStageMachine} 控制；
 * 流转规则：APPLIED → SCREENING_PASS → PHONE_INTERVIEW → TECH_INTERVIEW
 *           → HR_INTERVIEW → OFFER → HIRED；任意非终态可流转至 REJECTED。
 * 终态：HIRED 与 REJECTED。
 */
public enum ApplicationStage {
    APPLIED,
    SCREENING_PASS,
    PHONE_INTERVIEW,
    TECH_INTERVIEW,
    HR_INTERVIEW,
    OFFER,
    HIRED,
    REJECTED
}
