package com.fruit.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class Retailer extends PageEntity {

  private String retailerId;

  private String name;

  private String telephone;

  private String address;

  private Integer status;

  private String createTime;

}
