package com.dorm.entity.dorm.access;

import com.dorm.enums.dorm.access.AccessType;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AddAccessDTO {

    @NotNull(message = "学生id不能为空")
    private Integer studentId;

    @NotNull(message = "出入校类型不能为空")
    private AccessType type;

    @NotEmpty(message = "原因不能为空")
    private String reason;

    private String source;

    private String destination;

}
