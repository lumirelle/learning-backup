package com.dorm.entity.dorm;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class AddDormDTO {

    /**
     * 宿舍号，如：0709
     */
    @NotBlank(message = "宿舍号不能为空")
    private String no;

    /**
     * 宿舍楼，如：1 栋
     */
    @NotBlank(message = "宿舍楼不能为空")
    private String building;

    @NotNull(message = "宿舍容量不能为空")
    @Positive(message = "宿舍容量必须为正数")
    private Integer people;

}
