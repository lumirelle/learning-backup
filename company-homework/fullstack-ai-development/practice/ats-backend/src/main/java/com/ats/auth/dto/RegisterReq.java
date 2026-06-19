package com.ats.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class RegisterReq {

    @Email(message = "邮箱格式不正确")
    @NotBlank(message = "邮箱不能为空")
    private String email;

    @NotBlank(message = "密码不能为空")
    @Size(min = 8, max = 72, message = "密码长度 8-72 位")
    private String password;

    @NotBlank(message = "姓名不能为空")
    @Size(max = 100, message = "姓名最多 100 字符")
    private String fullName;

    /** 候选人自选兴趣（可选） */
    private List<String> interests;
}
