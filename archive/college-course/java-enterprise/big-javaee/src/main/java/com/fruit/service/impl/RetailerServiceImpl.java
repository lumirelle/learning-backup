package com.fruit.service.impl;

import com.fruit.dao.RetailerDao;
import com.fruit.entity.Retailer;
import com.fruit.service.RetailerService;
import jakarta.annotation.Resource;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class RetailerServiceImpl implements RetailerService {

  @Resource
  private RetailerDao retailerDao;

  public Retailer get(Serializable id) {
    return retailerDao.get(id);
  }

  public List<Retailer> find(Map<String, Object> map) {
    return retailerDao.find(map);
  }

  public void insert(Retailer retailer) {
    retailerDao.insert(retailer);
  }

  public void update(Retailer retailer) {
    retailerDao.update(retailer);
  }

  public void delete(Serializable[] ids) {
    retailerDao.delete(ids);
  }

  public void deleteById(Serializable id) {
    retailerDao.deleteById(id);
  }

  public int count(Map<String, Object> map) {
    return retailerDao.count(map);
  }

}
