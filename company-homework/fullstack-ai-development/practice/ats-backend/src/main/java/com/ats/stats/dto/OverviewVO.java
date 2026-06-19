package com.ats.stats.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 招聘概览（本月维度，时区 = Asia/Shanghai）。
 *
 * <h3>HR 视角</h3>
 * 仅统计自己 created_by 的岗位上的活动；ADMIN 视角看全部。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OverviewVO {
    /** 本月新增投递（applications.applied_at 在本月） */
    private long newApplicationsThisMonth;
    /** 本月推进到 OFFER 的次数（stage_logs 中本月内 to_stage=OFFER 的条数） */
    private long offersThisMonth;
    /** 本月入职（stage_logs 中本月内 to_stage=HIRED 的条数） */
    private long hiresThisMonth;
    /** 当前在招岗位数（jobs.status=PUBLISHED, deleted_at IS NULL） */
    private long activeJobs;
}
