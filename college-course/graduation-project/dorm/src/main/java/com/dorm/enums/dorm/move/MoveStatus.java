package com.dorm.enums.dorm.move;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.baomidou.mybatisplus.annotation.IEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MoveStatus implements IEnum<Integer> {
    WAIT_INSTRUCTOR_AUDIT(0, "INSTRUCTOR_AUDIT", "待辅导员审核"),

    WAIT_OFFICE_AUDIT(1, "OFFICE_AUDIT", "待学办审核"),

    WAIT_AFFAIRS_AUDIT(2, "AFFAIRS_AUDIT", "待学工部审核"),

    PASS(3, "PASS", "通过"),

    REJECT(4, "REJECT", "拒绝"),

    CANCELLED(5, "CANCELLED", "已撤销"),
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
