package com.fruit.dao;

import com.fruit.entity.Retailer;
import java.util.Map;

public interface RetailerDao extends BaseDao<Retailer> {

  int count(Map<String, Object> map);

}
