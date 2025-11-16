package com.cool.modules.order.entity;

import com.cool.core.base.BaseEntity;
import com.mybatisflex.annotation.Table;
import com.tangzc.mybatisflex.autotable.annotation.ColumnDefine;

import java.math.BigDecimal;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;

/**
 * 订单统计
 */
@Getter
@Setter
@Table(value = "order_statistics", comment = "订单统计")
public class OrderStatisticsEntity extends BaseEntity<OrderStatisticsEntity> {

    @ColumnDefine(comment = "订单总数")
    private Integer totalOrders;

    @ColumnDefine(comment = "总金额")
    private BigDecimal totalAmount;

    // 实付总金额
    @ColumnDefine(comment = "实付总金额")
    private BigDecimal totalActualAmount;

    @ColumnDefine(comment = "退款数")
    private Long refundCount;

    @ColumnDefine(comment = "完成数")
    private Long completedCount;

    @ColumnDefine(comment = "取消数")
    private Long cancelledCount;

    @ColumnDefine(comment = "支付订单数")
    private Long paidOrders;

    @ColumnDefine(comment = "开始日期")
    private Date startDate;

    @ColumnDefine(comment = "结束日期")
    private Date endDate;

}