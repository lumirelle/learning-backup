package org.seagulls.fatlife.type;

import java.io.Serializable;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public enum WashingType implements Serializable {

  SELF_CLEANING(1, "桶自洁", "￥0.00"),
  DEHYDRATION_ONLY(2, "仅脱水", "￥0.09"),
  STANDARD_WASHING(3, "标准洗", "￥2.85"),
  EXTEND_WASHING(4, "大物洗", "￥3.85");

  private static final long serialVersionUID = 1L;

  private final int id;

  private final String str;

  private final String amount;

  WashingType(int id, String str, String amount) {
    this.id = id;
    this.str = str;
    this.amount = amount;
  }

  public static WashingType valueOfCost(String cost) {
    for (WashingType washingType : WashingType.values()) {
      if (washingType.amount.equals(cost)) {
        return washingType;
      }
    }
    return null;
  }

}
