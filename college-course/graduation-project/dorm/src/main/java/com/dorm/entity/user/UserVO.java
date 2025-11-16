package com.dorm.entity.user;

import com.dorm.enums.user.UserRoles;
import lombok.Data;
import lombok.NonNull;

import java.util.List;

@Data
public class UserVO {
    private Integer id;
    private String username;
    private String email;
    private String phone;
    private String avatar;

    private UserRoles role;

    public static UserVO valueOf(@NonNull UserPO user) {
        UserVO userVO = new UserVO();
        userVO.setId(user.getId());
        userVO.setUsername(user.getUsername());
        userVO.setEmail(user.getEmail());
        userVO.setPhone(user.getPhone());
        userVO.setAvatar(user.getAvatar());
        userVO.setRole(user.getRole());
        return userVO;
    }

    public static List<UserVO> valuesOf(@NonNull List<UserPO> users) {
        return users.stream()
                .map(UserVO::valueOf)
                .toList();
    }

}
