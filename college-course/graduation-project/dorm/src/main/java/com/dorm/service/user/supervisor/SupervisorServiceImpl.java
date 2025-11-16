package com.dorm.service.user.supervisor;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dorm.mapper.user.supervisor.SupervisorMapper;
import com.dorm.entity.user.supervisor.SupervisorPO;
import org.springframework.stereotype.Service;

@Service
public class SupervisorServiceImpl extends ServiceImpl<SupervisorMapper, SupervisorPO> implements SupervisorService {

    @Override
    public boolean isNoExist(String no) {
        QueryWrapper<SupervisorPO> qw = new QueryWrapper<>();
        qw.eq("no", no);
        return this.count(qw) > 0;
    }

}
