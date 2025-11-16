package com.dorm.enums.user;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.baomidou.mybatisplus.annotation.IEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UserSex implements IEnum<Integer> {
    // 前端传入的值应该是字符串类型的名字 "MALE" & "FEMALE"
    MALE(0, "MALE", "男"),

    FEMALE(1, "FEMALE", "女"),;

    // TODO: 其他角色
    // 校园卡管理员

    // EnumValue注解用于指定数据库存储的值
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
