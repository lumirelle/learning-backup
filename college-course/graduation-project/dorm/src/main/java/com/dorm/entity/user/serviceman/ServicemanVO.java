package com.dorm.entity.user.serviceman;

import com.dorm.entity.user.UserPO;
import com.dorm.enums.user.UserSex;
import com.dorm.utils.BeanConvertUtils;
import lombok.Data;
import lombok.NonNull;

@Data
public class ServicemanVO {
    // from ServicemanPO
    private Integer id;
    private String no;
    private String name;
    private UserSex sex;
    private Integer age;

    private Integer userId;

    // from userPO
    private String username;
    private String avatar;
    private String email;
    private String phone;

    public static ServicemanVO valueOf(
        @NonNull ServicemanPO serviceManPO,
        UserPO userPO
    ) {
        return BeanConvertUtils.convert(ServicemanVO.class, serviceManPO, userPO);
    }

}
