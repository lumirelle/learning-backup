package com.dorm.service.dorm.move;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dorm.enums.dorm.move.MoveStatus;
import com.dorm.mapper.dorm.move.MoveMapper;
import com.dorm.entity.dorm.move.MovePO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MoveServiceImpl extends ServiceImpl<MoveMapper, MovePO> implements MoveService {

    @Override
    public List<MovePO> listMoveByStudentName(String studentName) {
        return baseMapper.listByStudentName(studentName);
    }

    @Override
    public boolean isAnyIdNotExist(List<Integer> ids) {
        QueryWrapper<MovePO> qw = new QueryWrapper<>();
        qw.in("id", ids);
        return this.count(qw) != ids.size();
    }

    @Override
    public boolean isAnyIdNotCancelable(List<Integer> ids) {
        QueryWrapper<MovePO> qw = new QueryWrapper<>();
        qw.in("id", ids)
            .ne("status", MoveStatus.PASS)
            .ne("status", MoveStatus.REJECT)
            .ne("status", MoveStatus.CANCELLED);
        return this.count(qw) != ids.size();
    }
}
