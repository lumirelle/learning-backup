package com.ats.job.dto;

import com.ats.entity.JobLevel;
import com.ats.entity.JobWorkType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class JobCreateReq {

    @NotBlank
    @Size(max = 200)
    private String title;

    @Size(max = 50_000)
    private String description;

    /** 默认 FULL_TIME（前端不传也可） */
    private JobWorkType workType = JobWorkType.FULL_TIME;

    /** 默认 MID */
    private JobLevel level = JobLevel.MID;

    /** 月薪下限（元/月），null = 面议 */
    @PositiveOrZero
    private Integer salaryMin;

    /** 月薪上限（元/月），≥ salaryMin */
    @PositiveOrZero
    private Integer salaryMax;

    @Min(1)
    private Short headcount = 1;

    /**
     * M6：岗位必须挂在子部门（叶子节点）下；工作地点从 sub_department.location 继承，
     * 因此 jobs 表本身不再持有 location 字段，前端不再单独传。
     */
    @NotNull
    private Long subDepartmentId;

    /** 选中的标签 id 列表（候选自 GET /tags） */
    private List<Long> tagIds;
}
