package com.fruit.entity;


import lombok.Data;

@Data
public class PageEntity {

  private Integer currentPage = 1;

  private Integer startPage = 0;

  private Integer pageSize = 10;

}
