package com.oddy.demospringboot.mapper;

import com.oddy.demospringboot.entity.Account;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.One;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

// 无需配置 MapperScan，使用 @Mapper 注解即可
@Mapper
public interface AccountMapper {

  // MyBatis 关联查询
  @Results(id = "basic", value = {
      @Result(column = "detail_id", property = "accountDetail", one = @One(select = "com.oddy.demospringboot.mapper.AccountDetailMapper.selectById"))})
  @Select("select * from account where id = #{id}")
  Account selectById(Integer id);

  @ResultMap("basic")
  @Select("select * from account where name = #{name}")
  Account selectByName(String name);

  @Insert("insert into account(name, email, password) values(#{name}, #{email}, #{password})")
  void insert(Account account);

}
