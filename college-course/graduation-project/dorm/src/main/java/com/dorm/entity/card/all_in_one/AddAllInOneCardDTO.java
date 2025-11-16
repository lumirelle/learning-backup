package com.dorm.entity.card.all_in_one;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AddAllInOneCardDTO {

    @NotNull(message = "学生id不能为空")
    private Integer studentId;

    @NotBlank(message = "密码不能为空")
    private String password;

    @NotBlank(message = "确认密码不能为空")
    private String confirmPassword;

}
