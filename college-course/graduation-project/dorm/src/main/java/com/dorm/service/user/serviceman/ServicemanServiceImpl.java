package com.dorm.service.user.serviceman;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dorm.entity.user.card_manager.CardManagerPO;
import com.dorm.entity.user.serviceman.ServicemanPO;
import com.dorm.mapper.user.card_manager.CardManagerMapper;
import com.dorm.mapper.user.serviceman.ServicemanMapper;
import org.springframework.stereotype.Service;

@Service
public class ServicemanServiceImpl extends ServiceImpl<ServicemanMapper, ServicemanPO> implements ServicemanService {

    @Override
    public boolean isNoExist(String no) {
        QueryWrapper<ServicemanPO> qw = new QueryWrapper<>();
        qw.eq("no", no);
        return this.count(qw) > 0;
    }

}
