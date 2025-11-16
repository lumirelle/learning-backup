package com.dorm.entity.dorm.objects;

import com.dorm.enums.dorm.objects.ObjectsType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AddObjectsDTO {
    @NotNull(message = "物品类型不能为空")
    private ObjectsType type;

    @NotBlank(message = "物品描述不能为空")
    private String description;

    @NotNull(message = "学生id不能为空")
    private Integer studentId;
}
