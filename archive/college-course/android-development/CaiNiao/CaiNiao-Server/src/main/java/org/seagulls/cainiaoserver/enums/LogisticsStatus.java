package org.seagulls.cainiaoserver.enums;

public enum LogisticsStatus {
  // 待取件，派送中，运输中，未发货，已签收，未知
  AWAITING_PICKUP, ON_DELIVERY, IN_TRANSIT, UNSHIPPED, HAS_SIGNED, UNKNOWN;

  @Override
  public String toString() {
    return switch (this) {
      case AWAITING_PICKUP -> "待取件";
      case ON_DELIVERY -> "派送中";
      case IN_TRANSIT -> "运输中";
      case UNSHIPPED -> "未发货";
      case HAS_SIGNED -> "已签收";
      case UNKNOWN -> "未知";
    };
  }
}
