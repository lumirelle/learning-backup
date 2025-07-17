package com.fruit.mapper;


import com.fruit.entity.Retailer;
import java.util.List;
import java.util.Map;

public interface RetailerMapper {

  Retailer get(String retailerId);

  List<Retailer> find(Map<String, Object> map);

  int count(Map<String, Object> map);

  void insert(Retailer retailer);

  void update(Retailer retailer);

  void deleteById(String retailerId);

  void delete(String[] retailerId);

}
