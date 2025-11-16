package com.dorm.entity.user;

import com.baomidou.mybatisplus.annotation.TableName;
import com.dorm.enums.user.UserRoles;
import lombok.Data;

@Data
@TableName("user")
public class UserPO {
    private Integer id;
    private String username;
    private String password;
    private String email;
    private String phone;
    private String avatar;
    private UserRoles role;
}
