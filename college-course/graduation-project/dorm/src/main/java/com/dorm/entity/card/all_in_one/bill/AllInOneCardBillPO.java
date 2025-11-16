package com.dorm.entity.card.all_in_one.bill;

import com.baomidou.mybatisplus.annotation.TableName;
import com.dorm.entity.card.all_in_one.AllInOneCardPO;
import com.dorm.enums.card.all_in_one.bill.AllInOneCardBillUseCase;
import com.dorm.utils.BeanConvertUtils;
import lombok.Data;
import lombok.NonNull;

import java.math.BigDecimal;
import java.util.Date;

@Data
@TableName("card_all_in_one_bill")
public class AllInOneCardBillPO {

    private Integer id;

    private String no;

    private Integer allInOneCardId;

    private BigDecimal changeAmount;

    private AllInOneCardBillUseCase useCase;

    private Date createTime;



}
