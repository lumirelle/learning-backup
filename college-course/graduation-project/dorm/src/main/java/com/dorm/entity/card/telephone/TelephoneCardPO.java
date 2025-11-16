package com.dorm.entity.card.telephone;

import com.baomidou.mybatisplus.annotation.TableName;
import com.dorm.entity.card.all_in_one.AddAllInOneCardDTO;
import com.dorm.entity.user.student.StudentPO;
import com.dorm.enums.card.all_in_one.AllInOneCardStatus;
import com.dorm.enums.card.telephone.TelephoneCardOperator;
import com.dorm.enums.card.telephone.TelephoneCardStatus;
import com.dorm.utils.BeanConvertUtils;
import lombok.Data;
import lombok.NonNull;

import java.math.BigDecimal;
import java.util.Date;

@Data
@TableName("card_telephone")
public class TelephoneCardPO {

    private Integer id;

    private String telephone;

    private Integer studentId;

    private Date createTime;

    private BigDecimal mealPrice;

    private TelephoneCardOperator operator;

    private TelephoneCardStatus status;

    public static TelephoneCardPO valueOf(@NonNull AddTelephoneCardDTO dto) {
        return BeanConvertUtils.convert(TelephoneCardPO.class, dto);
    }

}
