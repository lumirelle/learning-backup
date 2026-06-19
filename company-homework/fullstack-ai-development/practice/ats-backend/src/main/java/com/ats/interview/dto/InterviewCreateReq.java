package com.ats.interview.dto;

import com.ats.entity.InterviewConclusion;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class InterviewCreateReq {

    @NotBlank(message = "面试轮次不能为空")
    @Size(max = 100, message = "面试轮次长度不能超过 100")
    private String round;

    @NotNull(message = "评分不能为空")
    @Min(value = 1, message = "评分最小为 1")
    @Max(value = 5, message = "评分最大为 5")
    private Short rating;

    @NotNull(message = "结论不能为空")
    private InterviewConclusion conclusion;

    @Size(max = 2000)
    private String strengths;

    @Size(max = 2000)
    private String weaknesses;

    @Size(max = 2000)
    private String notes;
}
