package com.fruit.service.impl;

import com.fruit.dao.UserDao;
import com.fruit.entity.User;
import com.fruit.service.UserService;
import jakarta.annotation.Resource;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

  @Resource
  private UserDao userDao;

  @Override
  public User get(Serializable id) {
    return userDao.get(id);
  }

  @Override
  public List<User> find(Map<String, Object> map) {
    return userDao.find(map);
  }

  @Override
  public void insert(User user) {
    userDao.insert(user);
  }

  @Override
  public void update(User user) {
    userDao.update(user);
  }

  @Override
  public void deleteById(Serializable id) {
    userDao.deleteById(id);
  }

  @Override
  public void delete(Serializable[] ids) {
    userDao.delete(ids);
  }

}
