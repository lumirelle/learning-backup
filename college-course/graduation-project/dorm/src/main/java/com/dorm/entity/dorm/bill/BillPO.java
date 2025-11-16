package com.dorm.entity.dorm.bill;

import com.baomidou.mybatisplus.annotation.TableName;
import com.dorm.utils.BeanConvertUtils;
import lombok.Data;
import lombok.NonNull;

import java.math.BigDecimal;

@Data
@TableName("dorm_bill")
public class BillPO {

    private Integer id;

    /**
     * 水费账单
     */
    private String waterNo;

    /**
     * 电费账单
     */
    private String electricityNo;

    private Integer dormId;

    private BigDecimal amount;

    public static BillPO valueOf(@NonNull AddBillDTO addBillDTO) {
        return BeanConvertUtils.convert(BillPO.class, addBillDTO);
    }

}
