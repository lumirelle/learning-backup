package com.ats.application.dto;

import com.ats.entity.ApplicationStage;
import com.ats.entity.JobStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;

/** 投递详情 VO：基础字段 + 阶段时间线 + 当前可流转目标。 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationDetailVO {

    private Long id;
    private Long jobId;
    private String jobTitle;
    private JobStatus jobStatus;

    /** M6：岗位所属组织（经 sub_department join 得到） */
    private Long subDepartmentId;
    private String subDepartmentName;
    private Long departmentId;
    private String departmentName;
    private Long rootOrgId;
    private String rootOrgName;
    /** 工作地点，来自 sub_departments.location */
    private String jobLocation;

    private Long candidateId;
    private String candidateName;
    private String candidateEmail;

    private ApplicationStage stage;
    private String resumeUrl;
    private Short yearsExp;
    private String phone;
    private String rejectReason;

    private OffsetDateTime appliedAt;
    private OffsetDateTime updatedAt;

    /** 阶段流转时间线（按 operatedAt 升序） */
    private List<StageLogVO> stageLogs;

    /** 当前可流转的下一阶段集合：HR(job owner) / Admin 才返回；候选人/其他人返回 null */
    private Set<ApplicationStage> allowedTransitions;
}
