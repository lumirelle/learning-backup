package com.oddy.demospringboot.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oddy.demospringboot.entity.Account;
import com.oddy.demospringboot.mapper.AccountPlusMapper;
import com.oddy.demospringboot.service.AccountService;
import org.springframework.stereotype.Service;

// MP 也提供了 ServiceImpl 的模板，Service 继承了 IService 之后，Service 的实现类必须要继承 ServiceImpl
// 两个泛型一个是实体类的继承了 BaseMapper 的 Mapper 类，另一个则是实体类
@Service
public class AccountServiceImpl extends ServiceImpl<AccountPlusMapper, Account>
    implements AccountService {

}
