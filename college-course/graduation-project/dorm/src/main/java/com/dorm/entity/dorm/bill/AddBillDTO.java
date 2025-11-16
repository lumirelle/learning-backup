package com.dorm.entity.dorm.bill;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AddBillDTO {

    @NotBlank(message = "水费账单号不为空")
    private String waterNo;

    @NotBlank(message = "电费账单号不为空")
    private String electricityNo;

    @NotNull(message = "宿舍不为空")
    private Integer dormId;

}
