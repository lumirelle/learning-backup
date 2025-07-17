package com.fruit.service;

import com.fruit.entity.Accessory;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

public interface AccessoryService {

  Accessory get(Serializable id);

  List<Accessory> find(Map<String, Object> map);

  void insert(Accessory commodities);

  void update(Accessory commodities);

  void delete(Serializable[] ids);

  void deleteById(Serializable id);

  int deleteByFruitId(Serializable fruitId);

}
