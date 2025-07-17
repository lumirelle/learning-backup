package com.fruit.dao.impl;

import com.fruit.dao.UserDao;
import com.fruit.entity.User;
import org.springframework.stereotype.Repository;

@Repository
public class UserDaoImpl extends BaseDaoImpl<User> implements UserDao {

  public UserDaoImpl() {
    super.setNs("com.fruit.mapper.UserMapper");
  }

}
