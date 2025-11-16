package com.dorm.service.user.card_manager;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dorm.mapper.user.card_manager.CardManagerMapper;
import com.dorm.entity.user.card_manager.CardManagerPO;
import org.springframework.stereotype.Service;

@Service
public class CardManagerServiceImpl extends ServiceImpl<CardManagerMapper, CardManagerPO> implements CardManagerService {

    //编号是否存在
    @Override
    public boolean isNoExist(String no) {
        QueryWrapper<CardManagerPO> qw = new QueryWrapper<>();
        qw.eq("no", no);
        return this.count(qw) > 0;
    }

}
