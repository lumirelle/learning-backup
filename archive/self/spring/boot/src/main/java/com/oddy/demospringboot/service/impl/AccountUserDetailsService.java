package com.oddy.demospringboot.service.impl;

import com.oddy.demospringboot.entity.Account;
import com.oddy.demospringboot.mapper.AccountMapper;
import jakarta.annotation.Resource;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AccountUserDetailsService implements UserDetailsService {

  @Resource
  private AccountMapper accountMapper;

  @Resource
  private PasswordEncoder passwordEncoder;

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    Account account = accountMapper.selectByName(username);
    if (account == null) {
      throw new UsernameNotFoundException("用户不存在");
    }
    return User.withUsername(account.getName())
        .password(account.getPassword()) // 这里的密码是数据库中存着的，加密后的密码
        .roles("user")
        .build();
  }

}
