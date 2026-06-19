package com.ats.entity;

/**
 * 岗位状态 5 态 enum，对应 PostgreSQL job_status type。
 * 合法流转矩阵见 {@link com.ats.job.JobStatusMachine}。
 */
public enum JobStatus {
    /** 草稿，HR 编辑中，候选人不可见 */
    DRAFT,
    /** 招聘中，候选人可见可投递 */
    PUBLISHED,
    /** 暂停收件，候选人可见但标记暂停 */
    PAUSED,
    /** 已关闭，候选人可见但不可投递 */
    CLOSED,
    /** 已归档，列表默认隐藏，可恢复为 DRAFT */
    ARCHIVED
}
