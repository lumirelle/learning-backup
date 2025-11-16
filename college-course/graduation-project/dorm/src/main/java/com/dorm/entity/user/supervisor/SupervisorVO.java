package com.dorm.entity.user.supervisor;

import com.dorm.enums.user.UserSex;
import com.dorm.entity.user.UserPO;
import com.dorm.utils.BeanConvertUtils;
import lombok.Data;

@Data
public class SupervisorVO {
    // from SupervisorPO
    private Integer id;
    private String no;
    private String name;
    private UserSex sex;
    private Integer age;
    private String building;

    private Integer userId;

    // from UserPO
    private String username;
    private String avatar;
    private String email;
    private String phone;

    public static SupervisorVO valueOf(SupervisorPO supervisorPO, UserPO userPO) {
        return BeanConvertUtils.convert(SupervisorVO.class, supervisorPO, userPO);
    }

}
