package com.dorm.entity.user.student;

import com.dorm.enums.user.UserSex;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class AddStudentDTO {
    @NotBlank(message = "姓名不能为空")
    private String name;

    @NotBlank(message = "学号不能为空")
    private String no;

    @NotNull(message = "性别不能为空")
    private UserSex sex;

    @Positive(message = "年龄必须为正数")
    private Integer age;

    @NotBlank(message = "专业不能为空")
    private String major;

    @NotBlank(message = "学院不能为空")
    private String college;

    // 和用户关联
    @NotNull(message = "用户不能为空")
    private Integer userId;

    // 和宿舍关联
    @NotNull(message = "宿舍不能为空")
    private Integer dormId;
}
