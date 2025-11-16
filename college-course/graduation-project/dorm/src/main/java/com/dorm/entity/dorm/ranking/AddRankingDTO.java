package com.dorm.entity.dorm.ranking;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class AddRankingDTO {

    @NotNull(message = "宿舍id不能为空")
    private Integer dormId;

    @NotNull(message = "卫生得分不能为空")
    @Positive(message = "卫生得分必须大于0")
    @Max(value = 25, message = "卫生得分不能大于25")
    private Integer healthScore;

    @NotNull(message = "美观得分不能为空")
    @Positive(message = "美观得分必须大于0")
    @Max(value = 25, message = "美观得分不能大于25")
    private Integer beautyScore;

    @NotNull(message = "安全得分不能为空")
    @Positive(message = "安全得分必须大于0")
    @Max(value = 25, message = "安全得分不能大于25")
    private Integer safetyScore;

    @NotNull(message = "氛围得分不能为空")
    @Positive(message = "氛围得分必须大于0")
    @Max(value = 25, message = "氛围得分不能大于25")
    private Integer atmosphereScore;

}
