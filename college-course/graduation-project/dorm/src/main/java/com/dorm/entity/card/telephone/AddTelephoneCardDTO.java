package com.dorm.entity.card.telephone;

import com.dorm.enums.card.telephone.TelephoneCardOperator;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class AddTelephoneCardDTO {
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String telephone;

    @NotNull(message = "学生id不能为空")
    private Integer studentId;

    @NotNull(message = "套餐价格不能为空")
    @Positive(message = "套餐价格必须大于0")
    private BigDecimal mealPrice;

    @NotNull(message = "套餐运营商不能为空")
    private TelephoneCardOperator operator;

    @NotNull(message = "是否赠送宽带选项不能为空")
    private Boolean isGiveBandwidth;

    private Integer bandwidthSpeed;

}
