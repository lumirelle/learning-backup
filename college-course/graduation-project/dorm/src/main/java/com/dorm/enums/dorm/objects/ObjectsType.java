package com.dorm.enums.dorm.objects;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.baomidou.mybatisplus.annotation.IEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ObjectsType implements IEnum<Integer> {
    LUGGAGE(0, "LUGGAGE", "行李"),

    EXPRESS_DELIVERY(1, "EXPRESS_DELIVERY", "快递"),

    OTHER(2, "OTHER", "其他"),;

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
