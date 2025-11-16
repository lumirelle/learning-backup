package com.dorm.entity.user.teacher;

import com.baomidou.mybatisplus.annotation.TableName;
import com.dorm.entity.user.UserPO;
import com.dorm.enums.user.UserSex;
import com.dorm.enums.user.teacher.TeacherType;
import com.dorm.utils.BeanConvertUtils;
import lombok.Data;
import lombok.NonNull;

@Data
@TableName("user_teacher")
public class TeacherPO {
    private Integer id;
    private String no;
    private String name;
    private UserSex sex;
    private Integer age;
    private String major;
    private String college;
    private TeacherType teacherType;

    // 和用户关联
    private Integer userId;

    public static TeacherPO valueOf(@NonNull AddTeacherDTO addInstructor) {
        return BeanConvertUtils.convert(TeacherPO.class, addInstructor);
    }

}
