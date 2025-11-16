package com.dorm.entity.user.serviceman;

import com.baomidou.mybatisplus.annotation.TableName;
import com.dorm.entity.user.UserPO;
import com.dorm.enums.user.UserSex;
import com.dorm.utils.BeanConvertUtils;
import lombok.Data;
import lombok.NonNull;

@Data
@TableName("user_serviceman")
public class ServicemanPO {
    private Integer id;
    private String no;
    private String name;
    private UserSex sex;
    private Integer age;

    // 和用户关联
    private Integer userId;

    public static ServicemanPO valueOf(@NonNull AddServicemanDTO addStudent) {
        return BeanConvertUtils.convert(ServicemanPO.class, addStudent);
    }

}
