package com.fruit.mapper;


import com.fruit.entity.Accessory;
import java.util.List;
import java.util.Map;

public interface AccessoryMapper {

  Accessory get(String accessoryId);

  List<Accessory> find(Map<String, Object> map);

  void insert(Accessory accessory);

  void update(Accessory accessory);

  void delete(String[] accessoryId);

  void deleteById(String accessoryId);

  void deleteByFruitId(String fruitId);

}
