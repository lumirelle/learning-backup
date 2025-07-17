package com.fruit.service;

import com.fruit.entity.Commodities;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

public interface CommoditiesService {

  Commodities get(Serializable id);

  List<Commodities> find(Map<String, Object> map);

  void insert(Commodities commodities);

  void update(Commodities commodities);

  void delete(Serializable[] ids);

  void deleteById(Serializable id);

  int count(Map<String, Object> map);

}
