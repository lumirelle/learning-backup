package com.dorm.entity.card.telephone;

import com.dorm.enums.card.telephone.TelephoneCardStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateTelephoneCardDTO {

    @NotNull(message = "电话卡id不能为空")
    private Integer id;

    @NotNull(message = "套餐状态不能为空")
    private TelephoneCardStatus status;

}
