package com.fruit.entity;

import java.util.List;
import lombok.Data;

@Data
public class CommoditiesVo {

  private String fruitId;

  private String name;

  private Double price;

  private String locality;

  private String number;

  private List<Accessory> accessoryList;

}
