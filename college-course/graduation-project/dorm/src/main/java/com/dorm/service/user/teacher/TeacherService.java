package com.dorm.service.user.teacher;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dorm.entity.user.teacher.TeacherPO;

public interface TeacherService extends IService<TeacherPO> {

    /**
     * 检查工号是否存在
     * @param no 工号
     * @return true: 存在 false: 不存在
     */
    boolean isNoExist(String no);

}
