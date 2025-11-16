package com.dorm.enums.card.telephone;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.baomidou.mybatisplus.annotation.IEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TelephoneCardStatus implements IEnum<Integer> {
    NORMAL(0, "NORMAL", "正常使用"),
    //欠费
    OVERDUE(1, "OVERDUE", "欠费"),
    //注销
    CANCEL(2, "CANCEL", "注销"),
    //停机
    SUSPEND(3, "SUSPEND", "停机"),
    ;

    @EnumValue
    private final int code;
    private final String name;
    private final String description;

    @Override
    public Integer getValue() {
        return code;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
