package com.dorm.enums.card.all_in_one;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.baomidou.mybatisplus.annotation.IEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AllInOneCardStatus implements IEnum<Integer> {
    NORMAL(0, "NORMAL", "正常"),

    APPLICATION(1, "APPLICATION", "申请中"),

    LOST(2, "LOST", "挂失"),

    WITHDRAW(3, "WITHDRAW", "注销"),
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
