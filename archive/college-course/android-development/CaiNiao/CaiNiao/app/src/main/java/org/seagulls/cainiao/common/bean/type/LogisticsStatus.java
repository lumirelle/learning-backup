package org.seagulls.cainiao.common.bean.type;

import androidx.annotation.NonNull;

public enum LogisticsStatus {
  // 待取件，派送中，运输中，未发货，已签收，未知
  AWAITING_PICKUP, ON_DELIVERY, IN_TRANSIT, UNSHIPPED, HAS_SIGNED, UNKNOWN;

  @NonNull
  @Override
  public String toString() {
    switch (this) {
      case AWAITING_PICKUP:
        return "待取件";
      case ON_DELIVERY:
        return "派送中";
      case IN_TRANSIT:
        return "运输中";
      case UNSHIPPED:
        return "未发货";
      case HAS_SIGNED:
        return "已签收";
      case UNKNOWN:
        return "未知";
      default:
        return "";
    }
  }
}
