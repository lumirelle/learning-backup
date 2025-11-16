package com.dorm.service.dorm.access;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dorm.entity.dorm.move.MovePO;
import com.dorm.enums.dorm.access.AccessStatus;
import com.dorm.enums.dorm.move.MoveStatus;
import com.dorm.mapper.dorm.access.AccessMapper;
import com.dorm.entity.dorm.access.AccessPO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;


@Slf4j
@Service
public class AccessServiceImpl extends ServiceImpl<AccessMapper, AccessPO> implements AccessService {


    @Override
    public boolean isAnyIdNotExist(List<Integer> ids) {
        QueryWrapper<AccessPO> qw = new QueryWrapper<>();
        qw.in("id", ids);
        return this.count(qw) != ids.size();
    }

    @Override
    public boolean isAnyIdNotCancelable(List<Integer> ids) {
        QueryWrapper<AccessPO> qw = new QueryWrapper<>();
        qw.in("id", ids)
            .ne("status", AccessStatus.PASS)
            .ne("status", AccessStatus.REJECT)
            .ne("status", AccessStatus.CANCEL);
        return this.count(qw) != ids.size();
    }

}
