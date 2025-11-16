package com.dorm.entity.dorm.access;

import com.dorm.enums.dorm.access.AccessStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateAccessDTO {
    @NotNull(message = "id不能为空")
    private Integer id;

    @NotNull(message = "状态不能为空")
    private AccessStatus status;

}
