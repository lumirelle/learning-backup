package com.fruit.dao.impl;

import com.fruit.dao.RetailerDao;
import com.fruit.entity.Retailer;
import java.util.Map;
import org.springframework.stereotype.Repository;

@Repository
public class RetailerDaoImpl extends BaseDaoImpl<Retailer> implements RetailerDao {

  public RetailerDaoImpl() {
    super.setNs("com.fruit.mapper.RetailerMapper");
  }

  public int count(Map<String, Object> map) {
    return this.getSqlSession().selectOne(this.getNs() + ".count", map);
  }

}
