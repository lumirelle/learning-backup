package com.oddy.demospringboot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.oddy.demospringboot.entity.Account;
import org.apache.ibatis.annotations.Mapper;

// MP 提供了 Mapper 的模板，我们可以在 MyBatis Mapper 的基础上，继承 BaseMapper，
// 其内置了一些基本的操作，泛型的类型为实体类类型
// MP 仅仅是对 Mybatis 的增强，其引入对 Mybatis 的使用没有影响
// MP 没有提供关联查询的操作，所以仍然使用 MyBatis 的关联查询方法
@Mapper
public interface AccountPlusMapper extends BaseMapper<Account> {

}
