package com.cool.modules.order.entity;

import com.cool.core.base.BaseEntity;
import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Table;
import com.tangzc.mybatisflex.autotable.annotation.ColumnDefine;
import org.checkerframework.checker.units.qual.C;
import org.dromara.autotable.annotation.Index;
import lombok.Getter;
import lombok.Setter;

/**
 * 订单详情
 */
@Getter
@Setter
@Table(value = "order_detail", comment = "订单详情")
public class OrderDetailEntity extends BaseEntity<OrderDetailEntity> {

    @Index
    @ColumnDefine(comment = "订单ID")
    private Long orderId;

    @ColumnDefine(comment = "服务时长（分钟）")
    private Integer serviceMinutes;

    @ColumnDefine(comment = "客户评价 0~5 分")
    private Integer customerEvaluation;

    @ColumnDefine(comment = "售后状态 0-无售后 1-待处理 2-处理完成", defaultValue = "0")
    private Integer afterSaleStatus;

}