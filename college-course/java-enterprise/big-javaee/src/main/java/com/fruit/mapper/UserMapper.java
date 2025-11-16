package com.fruit.mapper;

import com.fruit.entity.User;
import java.util.List;
import java.util.Map;

public interface UserMapper {

  User get(String userId);

  List<User> find(Map<String, Object> map);

  void insert(User user);

  void update(User user);

  void delete(String[] userId);

  void deleteById(String userId);

}
