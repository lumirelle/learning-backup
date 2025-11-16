package com.fruit.service;

import com.fruit.entity.User;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

public interface UserService {

  User get(Serializable id);

  List<User> find(Map<String, Object> map);

  void insert(User user);

  void update(User user);

  void deleteById(Serializable id);

  void delete(Serializable[] ids);

}
