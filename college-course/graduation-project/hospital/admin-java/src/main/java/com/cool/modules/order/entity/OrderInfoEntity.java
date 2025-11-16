package com.cool.modules.order.entity;

import com.cool.core.base.BaseEntity;
import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Table;
import com.tangzc.mybatisflex.autotable.annotation.ColumnDefine;
import com.tangzc.mybatisflex.autotable.annotation.UniIndex;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 订单信息
 */
@Getter
@Setter
@Table(value = "order_info", comment = "订单信息")
public class OrderInfoEntity extends BaseEntity<OrderInfoEntity> {

    @UniIndex
    @ColumnDefine(comment = "编号")
    private String orderNumber;

    @ColumnDefine(comment = "状态 0-待支付 1-已支付 2-待使用 3-已完成 4-已取消 5-已退款", defaultValue = "0")
    private Integer status;

    @ColumnDefine(comment = "总金额")
    private BigDecimal totalAmount;

    @ColumnDefine(comment = "实付金额")
    private BigDecimal actualAmount;

    @ColumnDefine(comment = "优惠金额")
    private BigDecimal discountAmount;

    @ColumnDefine(comment = "套餐 ID")
    private Long mealId;

    @Column(ignore = true)
    private String mealName;

    @Column
    private String mealCover;

    @ColumnDefine(comment = "患者ID")
    private Long patientId;

    @Column(ignore = true)
    private String patientName;

    @ColumnDefine(comment = "支付方式 0-微信 1-支付宝 2-线下银行卡 3-线下现金", defaultValue = "0")
    private Integer payType;

    @ColumnDefine(comment = "支付时间")
    private Date payTime;

    @ColumnDefine(comment = "就诊时间")
    private Date visitTime;

    @ColumnDefine(comment = "备注")
    private String remark;

    @ColumnDefine(comment = "核销码")
    private String verifyCode;

    @Column(ignore = true)
    private Boolean hasComplaint;
}
