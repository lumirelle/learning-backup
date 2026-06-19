package com.ats.job.dto;

import com.ats.entity.JobLevel;
import com.ats.entity.JobStatus;
import com.ats.entity.JobWorkType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.util.List;

/**
 * 岗位列表查询参数（query string）。
 * <p>
 * 用法：
 * <ul>
 *   <li><b>HR / Admin</b>：可传 {@code mine=true} 只看自己创建的，或传 {@code status} 数组过滤</li>
 *   <li><b>候选人</b>：service 内部强制 status=[PUBLISHED, PAUSED, CLOSED]，{@code includeArchived} 失效</li>
 * </ul>
 */
@Data
public class JobListReq {

    /** 模糊关键词：在 title 上走 ILIKE，在 description 上走 to_tsvector('english') @@ */
    private String keyword;

    /** 状态过滤（多选），null 或空 = 不过滤；候选人调用时会被强制覆写为 PUBLISHED */
    private List<JobStatus> status;

    private List<JobWorkType> workType;

    private List<JobLevel> level;

    /** 按标签 slug 过滤（多选），命中任一即可（OR） */
    private List<String> tagSlugs;

    /**
     * 按"上层部门"筛（命中该部门下所有子部门的岗位）。
     * M6 改造：departments 现在是中间节点，jobs 实际挂在 sub_departments 上，SQL 走 sd.parent_department_id = ?。
     */
    private Long departmentId;

    /** 按"子部门"精确筛（叶子节点） */
    private Long subDepartmentId;

    /** 工作地点模糊匹配（ILIKE %x%） —— M6 起从 sub_departments.location 取值 */
    private String location;

    /** 薪资下限过滤：jobs.salary_max >= salaryMin（即"上限 ≥ 该值"才能进入候选） */
    private Integer salaryMin;

    /** 薪资上限过滤：jobs.salary_min <= salaryMax（即"下限 ≤ 该值"才能进入候选） */
    private Integer salaryMax;

    /** 仅查询当前用户自己创建的岗位（HR 后台用） */
    private Boolean mine;

    /** HR 团队视角：本人岗位 + 绑定子部门下他人岗位（与 mine 互斥，mine 优先） */
    private Boolean team;

    /** 是否包含已归档（默认 false；候选人无效） */
    private Boolean includeArchived;

    @Min(1)
    private Integer page = 1;

    @Min(1)
    @Max(100)
    private Integer size = 20;

    /** publishedAt / createdAt / viewCount / salaryMax */
    private String sortBy = "publishedAt";

    /** asc / desc */
    private String sortOrder = "desc";
}
