package com.ats.application.dto;

import com.ats.entity.JobLevel;
import com.ats.entity.JobWorkType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.util.List;

/**
 * HR 招聘看板 query 参数。
 * <p>
 * 设计：当 {@code jobId} 给定时走单岗位看板（保留旧行为 + owner/admin 鉴权）；
 * 否则用其他字段过滤"哪些岗位的投递进入看板"。
 * <p>
 * 字段语义与 {@link com.ats.job.dto.JobListReq} 对齐，便于 service 直接拼装委托给
 * {@code jobMapper.selectFilteredJobIds(...)} —— 而不是新建一套独立的 mapper conditions。
 */
@Data
public class BoardQueryReq {

    /** 单岗位看板模式：给定则其余 filter 字段（除 itemsPerColumn）被忽略 */
    private Long jobId;

    // ────────── 多维筛选（jobId=null 时生效） ──────────

    /** 模糊关键词：title ILIKE + description tsvector @@（与 /jobs 行为一致） */
    private String keyword;

    /** 工作类型（多选） */
    private List<JobWorkType> workType;

    /** 级别（多选） */
    private List<JobLevel> level;

    /** 标签 slug 多选（OR） */
    private List<String> tagSlugs;

    /**
     * 按"上层部门"筛（M6 后语义：sub_departments.parent_department_id = ?）。
     * 命中该部门下所有子部门的岗位。
     */
    private Long departmentId;

    /** 按"子部门"精确筛（叶子节点） */
    private Long subDepartmentId;

    /** 工作地点模糊（ILIKE %x%） —— M6 起从 sub_departments.location 取值 */
    private String location;

    /** 薪资过滤：jobs.salary_max >= salaryMin */
    private Integer salaryMin;

    /** 薪资过滤：jobs.salary_min <= salaryMax */
    private Integer salaryMax;

    /** 每列最多返回多少条明细，默认 50；后端会 clamp 到 [1, 200] */
    @Min(1)
    @Max(200)
    private Integer itemsPerColumn = 50;

    /** 列内分页偏移（加载更多时使用，与 {@link #stage} 配合） */
    @Min(0)
    private Integer columnOffset = 0;

    /** 仅加载指定列（加载更多时必填） */
    private com.ats.entity.ApplicationStage stage;
}
