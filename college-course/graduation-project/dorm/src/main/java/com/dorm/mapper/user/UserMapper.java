package com.dorm.mapper.user;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dorm.entity.user.UserPO;
import org.springframework.stereotype.Repository;

@Repository
public interface UserMapper extends BaseMapper<UserPO> {
}
