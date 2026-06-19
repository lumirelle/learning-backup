package com.ats.stats.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 公开聚合统计（无需登录即可访问）—— 用于登录 / 注册页"水位"展示。
 *
 * <h3>隐私设计</h3>
 * <ul>
 *   <li>所有字段都是<strong>聚合数字</strong>，不暴露具体岗位 / 候选人 / HR 个人信息。</li>
 *   <li>前端会对小数（&lt; 5）模糊化为"多人 / 多个"展示，避免在数据稀疏时暴露具体到个位的人数。</li>
 *   <li>仅 GET，<code>permitAll</code>；不带任何参数。</li>
 * </ul>
 *
 * <h3>语义</h3>
 * 全部为<strong>当前快照</strong>（非"本月"维度），避免初始数据稀疏时全是 0 显得平台死气沉沉。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PublicStatsVO {
    /** 简历筛选阶段候选人数（APPLIED + SCREENING_PASS） */
    private long screeningCount;
    /** 面试阶段候选人数（PHONE_INTERVIEW + TECH_INTERVIEW + HR_INTERVIEW） */
    private long interviewCount;
    /** Offer 阶段候选人数（OFFER） */
    private long offerCount;
    /** 当前发布中的岗位数（jobs.status=PUBLISHED 且未软删） */
    private long publishedJobs;
    /** 已被在招岗位覆盖到的部门数（即"至少有 1 个 PUBLISHED 岗位"的部门数） */
    private long coveredDepartments;
}
