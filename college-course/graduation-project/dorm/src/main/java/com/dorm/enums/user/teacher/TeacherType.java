package com.dorm.enums.user.teacher;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.baomidou.mybatisplus.annotation.IEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TeacherType implements IEnum<Integer> {
    INSTRUCTOR(0, "INSTRUCTOR", "辅导员"),

    OFFICE(1, "OFFICE", "学办"),

    AFFAIRS(2, "AFFAIRS", "学工部"),

    UNSET(3, "UNSET", "未设置"),;

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
