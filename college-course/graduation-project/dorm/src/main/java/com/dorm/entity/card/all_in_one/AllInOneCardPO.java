package com.dorm.entity.card.all_in_one;

import com.baomidou.mybatisplus.annotation.TableName;
import com.dorm.entity.user.student.StudentPO;
import com.dorm.enums.card.all_in_one.AllInOneCardStatus;
import com.dorm.utils.BeanConvertUtils;
import lombok.Data;
import lombok.NonNull;

import java.math.BigDecimal;

@Data
@TableName("card_all_in_one")
public class AllInOneCardPO {

    private Integer id;

    private Integer studentId;

    private String no;

    private String password;

    private BigDecimal amount;

    private AllInOneCardStatus status;

    public static AllInOneCardPO valueOf(@NonNull AddAllInOneCardDTO dto) {
        return BeanConvertUtils.convert(AllInOneCardPO.class, dto);
    }

}
