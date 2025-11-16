package com.dorm.enums.dorm.fix;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.baomidou.mybatisplus.annotation.IEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum FixType implements IEnum<Integer> {
    WATER(0, "WATER", "水"),

    ELECTRICITY(1, "ELECTRICITY", "电"),

    WALL(2, "WALL", "墙装"),

    OBJECT(3, "OBJECT", "物品"),

    OTHER(4, "OTHER", "其他"),;

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
