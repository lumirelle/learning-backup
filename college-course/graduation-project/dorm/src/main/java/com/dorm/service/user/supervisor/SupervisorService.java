package com.dorm.service.user.supervisor;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dorm.entity.user.supervisor.SupervisorPO;

public interface SupervisorService extends IService<SupervisorPO> {

    /**
     * 检查编号是否存在
     * @param no 编号
     * @return true: 存在 false: 不存在
     */
    boolean isNoExist(String no);

}
