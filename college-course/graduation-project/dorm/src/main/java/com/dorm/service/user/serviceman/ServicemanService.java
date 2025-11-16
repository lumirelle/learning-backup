package com.dorm.service.user.serviceman;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dorm.entity.user.card_manager.CardManagerPO;
import com.dorm.entity.user.serviceman.ServicemanPO;

public interface ServicemanService extends IService<ServicemanPO> {


    /**
     * 检查编号是否存在
     * @param no 编号
     * @return true: 存在 false: 不存在
     */
    boolean isNoExist(String no);

}
