package com.fruit.dao;

import com.fruit.entity.Contract;
import com.fruit.entity.ContractVo;
import com.fruit.entity.MiddleTab;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

public interface ContractDao extends BaseDao<Contract> {

  int count(Map<String, Object> map);

  List<ContractVo> findContractList(Map<String, Object> map);

  void insertMiddleTab(MiddleTab middleTab);

  void deleteMiddleTab(Serializable contractId);

  String getMaxBarCode();

}
