package com.fruit.service.impl;

import com.fruit.dao.CommoditiesDao;
import com.fruit.entity.Commodities;
import com.fruit.service.CommoditiesService;
import jakarta.annotation.Resource;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class CommoditiesServiceImpl implements CommoditiesService {

  @Resource
  CommoditiesDao commoditiesDao;

  public Commodities get(Serializable id) {
    return commoditiesDao.get(id);
  }

  public List<Commodities> find(Map<String, Object> map) {
    return commoditiesDao.find(map);
  }

  public void insert(Commodities commodities) {
    commoditiesDao.insert(commodities);
  }

  public void update(Commodities commodities) {
    commoditiesDao.update(commodities);
  }

  public void delete(Serializable[] ids) {
    commoditiesDao.delete(ids);
  }

  public void deleteById(Serializable id) {
    commoditiesDao.deleteById(id);
  }

  public int count(Map<String, Object> map) {
    return commoditiesDao.count(map);
  }

}
