package com.fruit.dao.impl;

import com.fruit.dao.AccessoryDao;
import com.fruit.entity.Accessory;
import java.io.Serializable;
import org.springframework.stereotype.Repository;

@Repository
public class AccessoryDaoImpl extends BaseDaoImpl<Accessory> implements AccessoryDao {

  public AccessoryDaoImpl() {
    super.setNs("com.fruit.mapper.AccessoryMapper");
  }

  @Override
  public int deleteByFruitId(Serializable fruitId) {
    return getSqlSession().delete(getNs() + ".deleteByFruitId", fruitId);
  }

}
