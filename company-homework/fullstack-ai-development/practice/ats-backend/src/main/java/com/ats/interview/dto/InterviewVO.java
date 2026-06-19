package com.ats.interview.dto;

import com.ats.entity.InterviewConclusion;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

/**
 * 面试记录回显 VO，含面试官姓名 + 角色（join users）。
 * 同时返回 {@code editable} 让前端决定是否显示「编辑」按钮（24h 内 + 自己写的）。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InterviewVO {

    private Long id;
    private Long applicationId;

    private Long interviewerId;
    private String interviewerName;
    private String interviewerRole;

    private String round;
    private Short rating;

    private String strengths;
    private String weaknesses;

    private InterviewConclusion conclusion;
    private String notes;

    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    /**
     * 当前调用方是否能编辑此条记录。
     * <p>真值条件：当前用户 == interviewerId，且 createdAt 距今 ≤ 24h；ADMIN 不受时间限制。</p>
     * <p>由 service 在返回前根据 SecurityUtil 计算填入。</p>
     */
    private Boolean editable;
}
