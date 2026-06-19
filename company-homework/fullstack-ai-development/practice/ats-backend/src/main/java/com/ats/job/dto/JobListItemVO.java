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

/** 列表项 VO（瘦身版，不含 description）。 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobListItemVO {

    private Long id;
    private String title;
    private String location;
    private JobWorkType workType;
    private JobLevel level;
    private Integer salaryMin;
    private Integer salaryMax;
    /** 展示用，如 "30k-50k" / "薪资面议" */
    private String salaryRange;
    private Short headcount;
    private JobStatus status;
    private Integer viewCount;
    private OffsetDateTime publishedAt;
    private OffsetDateTime updatedAt;

    /**
     * M6：jobs 改挂子部门。VO 仍同时暴露 departmentId（上层部门 id）+ subDepartmentId，
     * 方便前端按需展示 "技术研发 / 技术研发-上海浦东 / 上海·浦东" 三段式。
     */
    private Long subDepartmentId;
    private String subDepartmentName;
    private Long departmentId;
    private String departmentName;
    private Long rootOrgId;
    private String rootOrgName;
    private Long createdBy;
    private String createdByName;

    /** 限制最多 5 个，避免列表过宽 */
    private List<TagVO> tags;
}
