package com.fruit.service;

import com.fruit.entity.Retailer;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

public interface RetailerService {

  Retailer get(Serializable id);

  List<Retailer> find(Map<String, Object> map);

  void insert(Retailer retailer);

  void update(Retailer retailer);

  void delete(Serializable[] ids);

  void deleteById(Serializable id);

  int count(Map<String, Object> map);

}
