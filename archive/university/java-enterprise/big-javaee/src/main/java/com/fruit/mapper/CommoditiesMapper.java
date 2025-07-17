package com.fruit.mapper;


import com.fruit.entity.Commodities;
import java.util.List;
import java.util.Map;


public interface CommoditiesMapper {

  Commodities get(String commoditiesId);

  List<Commodities> find(Map<String, Object> map);

  int count(Map<String, Object> map);

  void insert(Commodities commodities);

  void update(Commodities commodities);

  void delete(String[] commoditiesId);

  void deleteById(String commoditiesId);

}
