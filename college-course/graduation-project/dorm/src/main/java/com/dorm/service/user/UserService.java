package com.dorm.service.user;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dorm.entity.user.UserPO;

import java.util.List;

public interface UserService extends IService<UserPO> {
    /**
     * 查询未绑定的学生用户列表
     * @return 未绑定的学生用户列表
     */
    List<UserPO> listUnboundStudentUsers();

    /**
     * 查询未绑定的教师用户列表
     * @return 未绑定的教师用户列表
     */
    List<UserPO> listUnboundTeacherUsers();

    /**
     * 查询未绑定的宿管用户列表
     * @return 未绑定的宿管用户列表
     */
    List<UserPO> listUnboundSupervisorUsers();

    /**
     * 查询未绑定的校园卡管理员用户列表
     * @return 未绑定的校园卡管理员用户列表
     */
    List<UserPO> listUnboundCardManagerUsers();

    /**
     * 查询未绑定的维修人员用户列表
     * @return 未绑定的维修人员用户列表
     */
    List<UserPO> listUnboundServicemanUsers();

    /**
     * 查询用户名是否已经存在
     * @param username 用户名
     * @return true: 存在; false: 不存在
     */
    boolean isUserNameExist(String username);
}
