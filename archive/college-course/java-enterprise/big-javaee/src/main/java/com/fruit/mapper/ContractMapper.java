package com.fruit.mapper;

import com.fruit.entity.Contract;
import com.fruit.entity.ContractVo;
import com.fruit.entity.MiddleTab;
import java.util.List;
import java.util.Map;

public interface ContractMapper {

  Contract get(String contractId);

  List<ContractVo> findContractList(Map<String, Object> map);

  String getMaxBarCode(String contractId);

  void insert(Contract contract);

  void insertMiddleTab(MiddleTab middleTab);

  void update(Contract contract);

  void delete(String[] contractId);

  void deleteMiddleTab(String contractId);

  void deleteById(String contractId);

  int count(Map<String, Object> map);

}
