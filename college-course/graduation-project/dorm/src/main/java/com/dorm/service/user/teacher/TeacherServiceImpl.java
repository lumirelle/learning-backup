package com.dorm.service.user.teacher;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dorm.mapper.user.teacher.TeacherMapper;
import com.dorm.entity.user.teacher.TeacherPO;
import org.springframework.stereotype.Service;

@Service
public class TeacherServiceImpl extends ServiceImpl<TeacherMapper, TeacherPO> implements TeacherService {

    @Override
    public boolean isNoExist(String no) {
        QueryWrapper<TeacherPO> qw = new QueryWrapper<>();
        qw.eq("no", no);
        return this.count(qw) > 0;
    }

}
