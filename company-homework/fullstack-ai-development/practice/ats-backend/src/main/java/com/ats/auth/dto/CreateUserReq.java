package com.ats.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/** Admin 创建 HR / CANDIDATE 账号用的请求体 */
@Data
public class CreateUserReq {

    @Email(message = "邮箱格式不正确")
    @NotBlank(message = "邮箱不能为空")
    private String email;

    @NotBlank(message = "初始密码不能为空")
    @Size(min = 8, max = 72, message = "密码长度 8-72 位")
    private String password;

    @NotBlank(message = "姓名不能为空")
    @Size(max = 100, message = "姓名最多 100 字符")
    private String fullName;

    /** 只允许 HR 或 CANDIDATE，ADMIN 账号不能通过此接口创建 */
    @NotBlank
    @Pattern(regexp = "HR|CANDIDATE", message = "role 只能是 HR 或 CANDIDATE")
    private String role;

    /**
     * HR 绑定的子部门 id 列表（M6 多对多）。
     * role=HR 时 service 层校验至少 1 个；CANDIDATE 忽略。
     */
    private List<Long> subDepartmentIds;
}
