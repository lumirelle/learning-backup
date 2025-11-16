package com.dorm.entity.dorm.fix;

import com.dorm.enums.dorm.fix.FixType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AddFixDTO {
    @NotNull(message = "报修类型不能为空")
    private FixType type;

    @NotBlank(message = "报修描述不能为空")
    private String description;

    // 关联宿舍
    @NotNull(message = "宿舍 ID 不能为空")
    private Integer dormId;
}
