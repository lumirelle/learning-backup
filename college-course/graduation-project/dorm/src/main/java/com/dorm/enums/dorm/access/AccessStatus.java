package com.dorm.enums.dorm.access;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.baomidou.mybatisplus.annotation.IEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AccessStatus  implements IEnum<Integer> {

    WAIT_ADULT(0, "WAIT_ADULT", "待审核"),

    PASS(1, "PASS", "已通过"),

    REJECT(2, "REJECT", "已拒绝"),

    CANCEL(3, "CANCEL", "已取消"),;

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
