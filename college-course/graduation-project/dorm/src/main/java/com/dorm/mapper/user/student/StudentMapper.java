package com.dorm.mapper.user.student;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dorm.entity.user.student.StudentPO;
import org.springframework.stereotype.Repository;

@Repository
public interface StudentMapper extends BaseMapper<StudentPO> {
}
