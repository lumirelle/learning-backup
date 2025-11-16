package com.dorm.entity.user.student;

import com.dorm.enums.user.UserSex;
import com.dorm.entity.dorm.DormPO;
import com.dorm.entity.user.UserPO;
import com.dorm.utils.BeanConvertUtils;
import lombok.Data;
import lombok.NonNull;

import java.util.List;

@Data
public class StudentVO {
    // from studentPO
    private Integer id;
    private String no;
    private String name;
    private UserSex sex;
    private Integer age;
    private String major;
    private String college;

    private Integer userId;
    private Integer dormId;

    // from userPO
    private String username;
    private String avatar;
    private String email;
    private String phone;

    // from dormPO
    private String dorm;

    public static StudentVO valueOf(
        @NonNull StudentPO studentPO
    ) {
        return valueOf(studentPO, null, null);
    }

    public static StudentVO valueOf(
        @NonNull StudentPO studentPO,
        UserPO userPO
    ) {
        return valueOf(studentPO, userPO, null);
    }

    public static StudentVO valueOf(
        @NonNull StudentPO studentPO,
        DormPO dormPO
    ) {
        return valueOf(studentPO, null, dormPO);
    }

    public static StudentVO valueOf(
        @NonNull StudentPO studentPO,
        UserPO userPO,
        DormPO dormPO
    ) {
        // 使用BeanConvertUtils工具类进行同名字段转换
        StudentVO studentVO = BeanConvertUtils.convert(StudentVO.class, studentPO, userPO, dormPO);
        // 特殊字段处理
        if (dormPO != null) {
            studentVO.setDorm(dormPO.getBuilding() + " " + dormPO.getNo());
        }
        return studentVO;
    }

    public static List<StudentVO> valuesOf(@NonNull List<StudentPO> studentPOList) {
        return studentPOList.stream().map(StudentVO::valueOf).toList();
    }

}
