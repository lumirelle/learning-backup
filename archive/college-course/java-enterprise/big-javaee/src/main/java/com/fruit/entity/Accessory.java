package com.fruit.entity;


import lombok.Data;

@Data
public class Accessory {

  private String accessoryId;

  private String fruitId;

  private String name;

  private double price;

  private String createTime;

}