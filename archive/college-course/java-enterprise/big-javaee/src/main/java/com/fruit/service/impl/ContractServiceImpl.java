package com.fruit.service.impl;

import com.fruit.dao.ContractDao;
import com.fruit.entity.Contract;
import com.fruit.entity.ContractVo;
import com.fruit.entity.MiddleTab;
import com.fruit.service.ContractService;
import jakarta.annotation.Resource;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class ContractServiceImpl implements ContractService {

  @Resource
  private ContractDao contractDao;

  @Override
  public Contract get(Serializable id) {
    return contractDao.get(id);
  }

  @Override
  public List<Contract> find(Map<String, Object> map) {
    return contractDao.find(map);
  }

  @Override
  public List<ContractVo> findContractList(Map<String, Object> map) {
    return contractDao.findContractList(map);
  }

  @Override
  public int count(Map<String, Object> map) {
    return contractDao.count(map);
  }

  @Override
  public void insert(Contract contract, String[] commodityIdArrays, String[] priceArrays) {
    contractDao.insert(contract);
    for (int i = 0; i < commodityIdArrays.length; i++) {
      MiddleTab middleTab = new MiddleTab();
      middleTab.setMiddleId(UUID.randomUUID().toString());
      middleTab.setContractId(contract.getContractId());
      middleTab.setFruitId(commodityIdArrays[i]);
      int number = Integer.parseInt(priceArrays[i].isEmpty() ? "0" : priceArrays[i]);
      middleTab.setNumber(number);
      contractDao.insertMiddleTab(middleTab);
    }
  }

  @Override
  public void insertMiddleTab(MiddleTab middleTab) {
    contractDao.insertMiddleTab(middleTab);
  }

  @Override
  public void update(Contract contract, String[] commodityIdArrays, String[] priceArrays) {
    contractDao.update(contract);
    contractDao.deleteMiddleTab(contract.getContractId());
    for (int i = 0; i < commodityIdArrays.length; i++) {
      MiddleTab middleTab = new MiddleTab();
      middleTab.setMiddleId(UUID.randomUUID().toString());
      middleTab.setContractId(contract.getContractId());
      middleTab.setFruitId(commodityIdArrays[i]);
      int number = Integer.parseInt(priceArrays[i].isEmpty() ? "0" : priceArrays[i]);
      middleTab.setNumber(number);
      contractDao.insertMiddleTab(middleTab);
    }
  }

  @Override
  public void delete(Serializable[] ids) {
    contractDao.delete(ids);
  }

  @Override
  public void deleteById(Serializable id) {
    contractDao.deleteById(id);
    contractDao.deleteMiddleTab(id);
  }

  @Override
  public void deleteMiddleTab(Serializable id) {
    contractDao.deleteMiddleTab(id);
  }

  @Override
  public String getMaxBarCode() {
    return contractDao.getMaxBarCode();
  }

}
