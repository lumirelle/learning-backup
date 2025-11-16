package com.dorm.enums.dorm.move;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.baomidou.mybatisplus.annotation.IEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MoveType implements IEnum<Integer> {
    CHANGE(0, "CHANGE", "换宿舍"),

    DAY_SCHOOL(1, "DAY_SCHOOL", "走读"),

    GRADUATION_OR_WITHDRAW(2, "GRADUATION_OR_WITHDRAW", "毕业或退学"),
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
