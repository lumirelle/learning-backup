package com.fruit.dao.impl;

import com.fruit.dao.ContractDao;
import com.fruit.entity.Contract;
import com.fruit.entity.ContractVo;
import com.fruit.entity.MiddleTab;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Repository;

@Repository
public class ContractDaoImpl extends BaseDaoImpl<Contract> implements ContractDao {

  public ContractDaoImpl() {
    super.setNs("com.fruit.mapper.ContractMapper");
  }

  @Override
  public int count(Map<String, Object> map) {
    return super.getSqlSession().selectOne(super.getNs() + ".count", map);
  }

  @Override
  public List<ContractVo> findContractList(Map<String, Object> map) {
    return super.getSqlSession().selectList(super.getNs() + ".findContractList", map);
  }

  @Override
  public void insertMiddleTab(MiddleTab middleTab) {
    super.getSqlSession().insert(super.getNs() + ".insertMiddleTab", middleTab);
  }

  @Override
  public void deleteMiddleTab(Serializable contractId) {
    super.getSqlSession().delete(super.getNs() + ".deleteMiddleTab", contractId);
  }

  @Override
  public String getMaxBarCode() {
    return super.getSqlSession().selectOne(super.getNs() + ".getMaxBarCode");
  }

}
