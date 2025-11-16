package com.cool.modules.order.dto;

import java.util.Date;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@Schema(description = "订单统计")
public class OrderStatisticsDto {

  @Schema(description = "开始日期")
  @NotBlank
  private Date startDate;
  
  @Schema(description = "结束日期")
  @NotBlank
  private Date endDate;
  
}
