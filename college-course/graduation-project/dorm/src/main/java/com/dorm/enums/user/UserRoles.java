package com.dorm.enums.user;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.baomidou.mybatisplus.annotation.IEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UserRoles implements IEnum<Integer> {
    ADMIN(0, "ADMIN", "管理员"),

    STUDENT(1, "STUDENT", "学生"),

    TEACHER(2, "TEACHER", "老师"),

    SUPERVISOR(3, "SUPERVISOR", "宿管"),

    CARD_MANAGER(4, "CARD_MANAGER", "校园卡管理员"),

    SERVICEMAN(5, "SERVICEMAN", "维修人员"),
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
