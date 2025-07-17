package org.seagulls.fatlife.entity;

import java.io.Serializable;
import lombok.Data;
import org.seagulls.fatlife.type.WashingType;

@Data
public class Order implements Serializable {

  private static final long serialVersionUID = 1L;

  private final int id;

  private final String no;

  private final String makeTime;

  private final String finishTime;

  private final String payTime;

  private final WashingMachine washingMachine;

  private final WashingType washingType;

  public Order(String no, String makeTime, String finishTime, String payTime,
      WashingMachine washingMachine, WashingType washingType) {
    this(0, no, makeTime, finishTime, payTime, washingMachine, washingType);
  }

  public Order(int id, String no, String makeTime, String finishTime,
      String payTime, WashingMachine washingMachine, WashingType washingType) {
    this.id = id;
    this.no = no;
    this.makeTime = makeTime;
    this.finishTime = finishTime;
    this.payTime = payTime;
    this.washingMachine = washingMachine;
    this.washingType = washingType;
  }

}
