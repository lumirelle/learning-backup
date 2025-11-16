package com.dorm.entity.card.bandwidth;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AddBandwidthDTO {

    @NotNull(message = "宽带速度不能为空")
    private Integer speed;

    @NotNull(message = "关联的校园电话卡不能为空")
    private Integer telephoneCardId;

}
