package com.dorm.service.dorm.move;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dorm.entity.dorm.move.MovePO;

import java.util.List;

public interface MoveService extends IService<MovePO> {

    List<MovePO> listMoveByStudentName(String studentName);

    boolean isAnyIdNotExist(List<Integer> ids);

    boolean isAnyIdNotCancelable(List<Integer> ids);

}
