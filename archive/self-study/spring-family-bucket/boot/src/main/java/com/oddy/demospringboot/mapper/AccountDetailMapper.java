package com.oddy.demospringboot.mapper;

import com.oddy.demospringboot.entity.AccountDetail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface AccountDetailMapper {

  @Select("select * from demo_spring_boot.account_detail where id = #{id}")
  AccountDetail selectById(Integer id);

}
