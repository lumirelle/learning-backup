package com.dorm.service.user.student;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dorm.mapper.user.student.StudentMapper;
import com.dorm.entity.user.student.StudentPO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class StudentServiceImpl extends ServiceImpl<StudentMapper, StudentPO> implements StudentService {

    @Override
    public boolean isNoExist(String no) {
        QueryWrapper<StudentPO> qw = new QueryWrapper<>();
        qw.eq("no", no);
        return this.count(qw) > 0;
    }

    @Override
    public boolean isAnyIdNotExist(List<Integer> ids) {
        QueryWrapper<StudentPO> qw = new QueryWrapper<>();
        qw.in("id", ids);
        return this.count(qw) != ids.size();
    }

    @Override
    public List<Integer> getDormIdsByStudentIds(List<Integer> studentIds) {
        QueryWrapper<StudentPO> qw = new QueryWrapper<>();
        qw.in("id", studentIds);
        return this.list(qw).stream().map(StudentPO::getDormId).collect(Collectors.toList());
    }

}
