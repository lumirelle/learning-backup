package com.dorm.entity.card.bandwidth;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateBandwidthDTO {

    @NotNull(message = "宽带ID不能为空")
    private Integer id;

    @NotNull(message = "宽带速度不能为空")
    private Integer speed;

}
