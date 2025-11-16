package com.dorm.entity.user.supervisor;

import com.baomidou.mybatisplus.annotation.TableName;
import com.dorm.entity.user.UserPO;
import com.dorm.enums.user.UserSex;
import com.dorm.utils.BeanConvertUtils;
import lombok.Data;

@Data
@TableName("user_supervisor")
public class SupervisorPO {
    private Integer id;
    private String no;
    private String name;
    private UserSex sex;
    private Integer age;
    private String building;

    // 和用户关联
    private Integer userId;

    public static SupervisorPO valueOf(AddSupervisorDTO addSupervisor) {
        return BeanConvertUtils.convert(SupervisorPO.class, addSupervisor);
    }

}
