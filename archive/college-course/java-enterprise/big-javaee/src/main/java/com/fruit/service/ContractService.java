package com.fruit.service;

import com.fruit.entity.Contract;
import com.fruit.entity.ContractVo;
import com.fruit.entity.MiddleTab;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

public interface ContractService {

  Contract get(Serializable id);

  List<Contract> find(Map<String, Object> map);

  List<ContractVo> findContractList(Map<String, Object> map);

  int count(Map<String, Object> map);

  void insert(Contract contract, String[] commodityIdArrays, String[] priceArrays);

  void insertMiddleTab(MiddleTab middleTab);

  void update(Contract contract, String[] commodityIdArrays, String[] priceArrays);

  void delete(Serializable[] ids);

  void deleteById(Serializable id);

  void deleteMiddleTab(Serializable id);

  String getMaxBarCode();

}
