package com.dorm.enums.dorm.fix;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.baomidou.mybatisplus.annotation.IEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum FixStatus  implements IEnum<Integer> {
    WAIT_FOR_RECEIVE(0, "WAIT_FOR_RECEIVE", "待接收"),

    RECEIVED(1, "RECEIVED", "已接收"),

    PROCESSED(2, "PROCESSED", "已处理"),

    CANCELLED(3, "CANCELLED", "已撤销"),;

    //与数据库中的整型字段直接映射
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
