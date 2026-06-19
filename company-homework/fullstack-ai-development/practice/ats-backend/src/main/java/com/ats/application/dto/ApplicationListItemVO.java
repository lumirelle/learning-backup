package com.ats.application.dto;

import com.ats.entity.ApplicationStage;
import com.ats.entity.JobStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

/**
 * 候选人「我的投递」列表项，HR 看板列表也共用（不同角色不同字段裁剪）。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationListItemVO {
    private Long id;
    private Long jobId;
    private String jobTitle;
    /** 岗位当前状态（候选人列表里展示，可能岗位已 PAUSED/CLOSED） */
    private JobStatus jobStatus;

    private Long candidateId;
    private String candidateName;
    private String candidateEmail;

    private ApplicationStage stage;
    private Short yearsExp;

    private OffsetDateTime appliedAt;
    private OffsetDateTime updatedAt;
}
