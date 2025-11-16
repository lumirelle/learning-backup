package com.dorm.service.dorm.access;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dorm.entity.dorm.access.AccessPO;

import java.util.List;

public interface AccessService extends IService<AccessPO> {

    boolean isAnyIdNotExist(List<Integer> ids);

    boolean isAnyIdNotCancelable(List<Integer> ids);

}
