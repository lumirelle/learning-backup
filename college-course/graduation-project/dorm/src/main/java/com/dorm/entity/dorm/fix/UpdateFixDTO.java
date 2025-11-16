package com.dorm.entity.dorm.fix;

import com.dorm.enums.dorm.fix.FixStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateFixDTO {
    @NotNull(message = "报修 ID 不能为空")
    private Integer id;

    @NotBlank(message = "报修描述不能为空")
    private String description;

    @NotNull(message = "报修状态不能为空")
    private FixStatus status;

}
