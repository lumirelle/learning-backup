package com.fruit.dao.impl;

import com.fruit.dao.CommoditiesDao;
import com.fruit.entity.Commodities;
import java.util.Map;
import org.springframework.stereotype.Repository;

@Repository
public class CommoditiesDaoImpl extends BaseDaoImpl<Commodities> implements CommoditiesDao {

  public CommoditiesDaoImpl() {
    super.setNs("com.fruit.mapper.CommoditiesMapper");
  }

  public int count(Map<String, Object> map) {
    return this.getSqlSession().selectOne(this.getNs() + ".count", map);
  }

}
