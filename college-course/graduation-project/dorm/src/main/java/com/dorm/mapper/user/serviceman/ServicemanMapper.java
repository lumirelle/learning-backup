package com.dorm.mapper.user.serviceman;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dorm.entity.user.card_manager.CardManagerPO;
import com.dorm.entity.user.serviceman.ServicemanPO;
import org.springframework.stereotype.Repository;

@Repository
public interface ServicemanMapper extends BaseMapper<ServicemanPO> {
}
