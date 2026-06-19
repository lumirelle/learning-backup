package com.ats.job.dto;

import com.ats.entity.JobLevel;
import com.ats.entity.JobStatus;
import com.ats.entity.JobWorkType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;

/** 详情页 VO（含 description 全文）。 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobDetailVO {

    private Long id;
    private String title;
    private String description;
    private String location;
    private JobWorkType workType;
    private JobLevel level;
    private Integer salaryMin;
    private Integer salaryMax;
    private String salaryRange;
    private Short headcount;
    private JobStatus status;
    private Integer viewCount;

    private OffsetDateTime publishedAt;
    private OffsetDateTime closedAt;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    /** M6：组织三段式（root / department / sub_department），便于前端面包屑展示 */
    private Long subDepartmentId;
    private String subDepartmentName;
    private Long departmentId;
    private String departmentName;
    private Long rootOrgId;
    private String rootOrgName;
    private Long createdBy;
    private String createdByName;

    private List<TagVO> tags;

    /** 当前用户对该岗位可执行的下一状态集合（HR/Admin 用，候选人为 null） */
    private Set<JobStatus> allowedTransitions;
}
