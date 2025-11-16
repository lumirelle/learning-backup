package com.dorm.entity.user.student;

import com.baomidou.mybatisplus.annotation.TableName;
import com.dorm.entity.dorm.DormPO;
import com.dorm.entity.user.UserPO;
import com.dorm.enums.user.UserSex;
import com.dorm.utils.BeanConvertUtils;
import lombok.Data;
import lombok.NonNull;

@Data
@TableName("user_student")
public class StudentPO {
    private Integer id;
    private String no;
    private String name;
    private UserSex sex;
    private Integer age;
    private String major;
    private String college;

    // 和用户关联
    private Integer userId;

    // 和宿舍关联
    private Integer dormId;

    public static StudentPO valueOf(@NonNull AddStudentDTO addStudent) {
        return BeanConvertUtils.convert(StudentPO.class, addStudent);
    }

}
