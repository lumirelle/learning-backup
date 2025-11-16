package com.dorm.entity.card.all_in_one.bill;

import com.dorm.entity.card.all_in_one.AllInOneCardPO;
import com.dorm.entity.user.student.StudentPO;
import com.dorm.enums.card.all_in_one.bill.AllInOneCardBillUseCase;
import com.dorm.utils.BeanConvertUtils;
import lombok.Data;
import lombok.NonNull;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class AllInOneCardBillVO {
    // from AllInOneCardBillPO
    private Integer id;
    private String no;
    private BigDecimal changeAmount;
    private AllInOneCardBillUseCase useCase;
    private Date createTime;
    private Integer allInOneCardId;

    // from AllInOneCardPO
    private String allInOneCardNo;

    // from StudentPO
    private String studentNo;
    private String name;

    public static AllInOneCardBillVO valueOf(
        @NonNull AllInOneCardBillPO allInOneCardBillPO,
        AllInOneCardPO allInOneCardPO,
        StudentPO studentPO
    ) {
        AllInOneCardBillVO allInOneCardBillVO = BeanConvertUtils.convert(
            AllInOneCardBillVO.class,
            allInOneCardBillPO,
            allInOneCardPO,
            studentPO
        );
        // 特殊字段，名称不一致
        if (allInOneCardPO != null) {
            allInOneCardBillVO.setAllInOneCardNo(allInOneCardPO.getNo());
        }
        if (studentPO != null) {
            allInOneCardBillVO.setStudentNo(studentPO.getNo());
        }
        return allInOneCardBillVO;
    }

}
