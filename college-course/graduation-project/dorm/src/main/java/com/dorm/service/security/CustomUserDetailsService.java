package com.dorm.service.security;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dorm.entity.user.UserPO;
import com.dorm.service.user.UserService;
import jakarta.annotation.Resource;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Resource
    private UserService userService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        QueryWrapper<UserPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        UserPO user = userService.getOne(queryWrapper);

        if (user == null) {
            throw new UsernameNotFoundException("用户不存在");
        }

        String role = user.getRole().name();

        return User.builder()
            .username(user.getUsername())
            .password(user.getPassword())
            .roles(role)
            .build();
    }
}
