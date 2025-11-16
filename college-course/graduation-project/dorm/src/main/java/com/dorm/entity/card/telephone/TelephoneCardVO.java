package com.dorm.entity.card.telephone;

import com.dorm.entity.user.UserPO;
import com.dorm.entity.user.student.StudentPO;
import com.dorm.enums.card.telephone.TelephoneCardOperator;
import com.dorm.enums.card.telephone.TelephoneCardStatus;
import com.dorm.utils.BeanConvertUtils;
import lombok.Data;
import lombok.NonNull;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
public class TelephoneCardVO {
    // from TelephoneCardPO
    private Integer id;
    private String telephone;
    private Integer studentId;
    private Date createTime;
    private BigDecimal mealPrice;
    private TelephoneCardOperator operator;
    private TelephoneCardStatus status;

    // from StudentPO
    private String no;
    private String name;

    // from UserPO
    /**
     * 办卡留的联系方式
     */
    private String phone;

    public static TelephoneCardVO valueOf(@NonNull TelephoneCardPO telephoneCardPO) {
        return valueOf(telephoneCardPO, null, null);
    }

    public static TelephoneCardVO valueOf(
        @NonNull TelephoneCardPO telephoneCardPO,
        StudentPO studentPO,
        UserPO userPO
    ) {
        return BeanConvertUtils.convert(TelephoneCardVO.class, telephoneCardPO, studentPO, userPO);
    }

    public static List<TelephoneCardVO> valuesOf(@NonNull List<TelephoneCardPO> telephoneCardPOList) {
        return telephoneCardPOList.stream().map(TelephoneCardVO::valueOf).toList();
    }

}
