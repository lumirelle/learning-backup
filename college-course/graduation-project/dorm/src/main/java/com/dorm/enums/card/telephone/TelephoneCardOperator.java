package com.dorm.enums.card.telephone;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.baomidou.mybatisplus.annotation.IEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TelephoneCardOperator implements IEnum<Integer> {
    MOBILE(0, "MOBILE", "中国移动"),

    UNICOM(1, "UNICOM", "中国联通"),

    TELECOM(2, "TELECOM", "中国电信"),

    CABLE(3, "CABLE", "中国广电"),
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



