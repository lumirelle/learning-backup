package com.fruit.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class ContractVo extends PageEntity {

  private String contractId;

  private String barCode;

  private String retailerName;

  private int type;

  private String createTime;

}
