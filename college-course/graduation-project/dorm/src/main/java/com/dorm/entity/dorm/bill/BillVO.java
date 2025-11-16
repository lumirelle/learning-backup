package com.dorm.entity.dorm.bill;

import com.dorm.entity.dorm.DormPO;
import com.dorm.utils.BeanConvertUtils;
import lombok.Data;
import lombok.NonNull;

import java.math.BigDecimal;

@Data
public class BillVO {

    // BillPO
    private Integer id;
    private String waterNo;
    private String electricityNo;
    private BigDecimal amount;
    private Integer dormId;

    // dormPO
    private String dorm;

    public static BillVO valueOf(@NonNull BillPO billPO, DormPO dormPO) {
        BillVO billVO = BeanConvertUtils.convert(BillVO.class, billPO);
        if (dormPO != null) {
            billVO.dorm = dormPO.getBuilding() + " " + dormPO.getNo();
        }
        return billVO;
    }

}
