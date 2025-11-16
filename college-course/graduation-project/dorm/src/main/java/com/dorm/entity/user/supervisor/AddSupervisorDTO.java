package com.dorm.entity.user.supervisor;

import com.dorm.enums.user.UserSex;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class AddSupervisorDTO {
    @NotBlank(message = "姓名不能为空")
    private String name;

    @NotBlank(message = "宿管编号不能为空")
    private String no;

    @NotNull(message = "性别不能为空")
    private UserSex sex;

    @Positive(message = "年龄必须为正数")
    private Integer age;

    @NotBlank(message = "所管楼栋不能为空")
    private String building;

    // 和用户关联
    @NotNull(message = "用户不能为空")
    private Integer userId;

}
