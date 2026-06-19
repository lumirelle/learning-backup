package com.ats.interview.dto;

import com.ats.entity.InterviewConclusion;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 编辑面试评价 · 当日窗口内（24h）由 service 层判定。
 * 字段与 create 一致；不允许改 application_id / interviewer_id（避免误改归属）。
 */
@Data
public class InterviewUpdateReq {

    @NotBlank
    @Size(max = 100)
    private String round;

    @NotNull
    @Min(1) @Max(5)
    private Short rating;

    @NotNull
    private InterviewConclusion conclusion;

    @Size(max = 2000)
    private String strengths;

    @Size(max = 2000)
    private String weaknesses;

    @Size(max = 2000)
    private String notes;
}
