package com.dorm.service.dorm.fix;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dorm.entity.dorm.fix.FixPO;

import java.util.List;

public interface FixService extends IService<FixPO> {

    boolean isAnyIdNotExist(List<Integer> ids);

    boolean isAnyIdNotCancelable(List<Integer> ids);

}
