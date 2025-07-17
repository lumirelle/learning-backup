package com.fruit.service.impl;

import com.fruit.dao.AccessoryDao;
import com.fruit.entity.Accessory;
import com.fruit.service.AccessoryService;
import jakarta.annotation.Resource;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class AccessoryServiceImpl implements AccessoryService {

  @Resource
  private AccessoryDao accessoryDao;

  public Accessory get(Serializable id) {
    return accessoryDao.get(id);
  }

  public List<Accessory> find(Map<String, Object> map) {
    return accessoryDao.find(map);
  }

  public void insert(Accessory accessory) {
    accessoryDao.insert(accessory);
  }

  public void update(Accessory accessory) {
    accessoryDao.update(accessory);
  }

  public void delete(Serializable[] ids) {
    accessoryDao.delete(ids);
  }

  public void deleteById(Serializable id) {
    accessoryDao.deleteById(id);
  }

  public int deleteByFruitId(Serializable fruitId) {
    return accessoryDao.deleteByFruitId(fruitId);
  }

}
