package com.dorm.entity.card.all_in_one;

import com.dorm.entity.user.UserPO;
import com.dorm.entity.user.student.StudentPO;
import com.dorm.enums.card.all_in_one.AllInOneCardStatus;
import com.dorm.utils.BeanConvertUtils;
import lombok.Data;
import lombok.NonNull;

import java.math.BigDecimal;
import java.util.List;

@Data
public class AllInOneCardVO {
    // from AllInOneCardPO
    private Integer id;
    private String no;
    private BigDecimal amount;
    private AllInOneCardStatus status;
    private Integer studentId;

    // from StudentPO
    private String studentNo;
    private String name;
    private String college;
    private String major;

    // from UserPO
    private String username;
    private String phone;
    private String email;

    public static List<AllInOneCardVO> valuesOf(@NonNull List<AllInOneCardPO> allInOneCardPOList) {
        return allInOneCardPOList.stream().map(AllInOneCardVO::valueOf).toList();
    }

    public static AllInOneCardVO valueOf(@NonNull AllInOneCardPO allInOneCardPO) {
        return valueOf(allInOneCardPO, null, null);
    }

    public static AllInOneCardVO valueOf(@NonNull AllInOneCardPO allInOneCardPO, StudentPO studentPO, UserPO userPO) {
        AllInOneCardVO allInOneCardVO = BeanConvertUtils.convert(
            AllInOneCardVO.class,
            allInOneCardPO,
            studentPO,
            userPO
        );
        if (studentPO != null) {
            // 特殊字段，名称不一致
            allInOneCardVO.setStudentNo(studentPO.getNo());
        }
        return allInOneCardVO;
    }

}
