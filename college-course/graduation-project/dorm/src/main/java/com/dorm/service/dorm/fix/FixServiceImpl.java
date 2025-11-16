package com.dorm.service.dorm.fix;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dorm.enums.dorm.fix.FixStatus;
import com.dorm.mapper.dorm.fix.FixMapper;
import com.dorm.entity.dorm.fix.FixPO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FixServiceImpl extends ServiceImpl<FixMapper, FixPO>implements FixService {

    @Override
    public boolean isAnyIdNotExist(List<Integer> ids) {
        QueryWrapper<FixPO> qw = new QueryWrapper<>();
        qw.in("id", ids);
        return this.count(qw) != ids.size();
    }

    @Override
    public boolean isAnyIdNotCancelable(List<Integer> ids) {
        QueryWrapper<FixPO> qw = new QueryWrapper<>();
        qw.in("id", ids).eq("status", FixStatus.WAIT_FOR_RECEIVE);
        return this.count(qw) != ids.size();
    }

}
