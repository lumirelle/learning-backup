package com.oddy.demoss2.mapper;

import com.oddy.demoss2.entity.Account;
import org.apache.ibatis.annotations.Select;

// 1.2.3 自定义认证 映射类
public interface AccountMapper {

  @Select("select * from users where username = #{username}")
  Account selectByUsername(String username);

}
