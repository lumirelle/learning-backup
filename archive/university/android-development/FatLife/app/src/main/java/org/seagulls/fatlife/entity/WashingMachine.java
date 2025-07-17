package org.seagulls.fatlife.entity;

import java.io.Serializable;
import lombok.Data;

@Data
public class WashingMachine implements Serializable {

  private static final long serialVersionUID = 1L;

  private final int id;

  private final String name;

  private final String shop;

  public WashingMachine(String name, String shop) {
    this(0, name, shop);
  }

  public WashingMachine(int id, String name, String shop) {
    this.id = id;
    this.name = name;
    this.shop = shop;
  }

}
