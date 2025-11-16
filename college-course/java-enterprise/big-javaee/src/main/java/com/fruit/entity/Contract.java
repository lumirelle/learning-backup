package com.fruit.entity;

import java.util.List;
import lombok.Data;

@Data
public class Contract {

  private String contractId;

  private String barCode;

  private Retailer retailer;

  private List<CommoditiesVo> commoditiesList;

  private int type;

  private String createTime;

}
