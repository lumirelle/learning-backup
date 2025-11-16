package com.dorm.entity.user.teacher;

import com.dorm.enums.user.UserSex;
import com.dorm.enums.user.teacher.TeacherType;
import com.dorm.entity.user.UserPO;
import com.dorm.utils.BeanConvertUtils;
import lombok.Data;
import lombok.NonNull;

@Data
public class TeacherVO {
    // from teacherPO
    private Integer id;
    private String no;
    private String name;
    private UserSex sex;
    private Integer age;
    private String major;
    private String college;
    private TeacherType teacherType;

    private Integer userId;

    // from userPO
    private String username;
    private String avatar;
    private String email;
    private String phone;

    public static TeacherVO valueOf(
        @NonNull TeacherPO teacherPO,
        UserPO userPO
    ) {
        return BeanConvertUtils.convert(TeacherVO.class, teacherPO, userPO);
    }

}
