package com.dorm.mapper.dorm.move;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dorm.entity.dorm.move.MovePO;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MoveMapper extends BaseMapper<MovePO> {

    List<MovePO> listByStudentName(String studentName);

}
