package com.fruit.dao;

import com.fruit.entity.Accessory;
import java.io.Serializable;

public interface AccessoryDao extends BaseDao<Accessory> {

  int deleteByFruitId(Serializable fruitId);

}
