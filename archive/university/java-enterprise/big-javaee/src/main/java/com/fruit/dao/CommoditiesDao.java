package com.fruit.dao;

import com.fruit.entity.Commodities;
import java.util.Map;

public interface CommoditiesDao extends BaseDao<Commodities> {

  int count(Map<String, Object> map);

}
