package com.fruit.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class Commodities extends PageEntity {

  private String fruitId;

  private String name;

  private Double price;

  private String locality;

  private String createTime;

}
