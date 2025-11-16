package com.dorm.service.user.student;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dorm.entity.user.student.StudentPO;

import java.util.List;

public interface StudentService extends IService<StudentPO> {

    /**
     * 检查学号是否存在
     * @param no 学号
     * @return true: 存在 false: 不存在
     */
    boolean isNoExist(String no);

    boolean isAnyIdNotExist(List<Integer> ids);

    List<Integer> getDormIdsByStudentIds(List<Integer> studentIds);

}
