package com.ats.auth.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * Admin 批量创建 HR / CANDIDATE 账号的请求体。
 * 列表内每条独立校验、独立提交、独立失败 —— 不整体回滚，
 * 这样部分行格式错误不会拖垮整批，前端可直接按行展示结果。
 */
@Data
public class BatchCreateUsersReq {

    @NotEmpty(message = "至少需要一条记录")
    @Size(max = 100, message = "单批最多 100 条，超出请分批提交")
    @Valid
    private List<CreateUserReq> users;
}
