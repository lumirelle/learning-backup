package com.oddy.demoss2.service.impl;

import com.oddy.demoss2.entity.Account;
import com.oddy.demoss2.mapper.AccountMapper;
import jakarta.annotation.Resource;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

// 1.2 自己编写实现类获取用户信息
// 自定义实现类其数据来源、获取的方式、获取的流程都是自定义的，
// 因此它的自定义程度高，适应不同的业务逻辑，适合实际开发使用
@Service
public class AccountUserDetailsService implements UserDetailsService {

  @Resource
  private AccountMapper accountMapper;

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    Account account = accountMapper.selectByUsername(username);
    if (account == null) {
      throw new UsernameNotFoundException("用户名或密码错误");
    }
    return User.withUsername(account.getUsername())
        // 使用数据库中加密了的密码，作认证对比用
        .password(account.getPassword())
        .roles(account.getRole())
        .build();
  }

}
