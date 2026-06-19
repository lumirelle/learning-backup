package com.ats.application.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ApplicationCreateReq {

    @NotNull(message = "岗位 ID 必填")
    private Long jobId;

    /** 简历 URL（M3 阶段允许候选人手填，M4 接入文件上传后改为后端写入）。 */
    @Size(max = 500, message = "简历地址过长")
    private String resumeUrl;

    @Min(value = 0, message = "工作年限不能为负数")
    private Short yearsExp;

    @Pattern(regexp = "^[0-9+\\-\\s]{0,30}$", message = "联系方式格式不合法")
    private String phone;
}
