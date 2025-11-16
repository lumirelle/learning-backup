package org.seagulls.cainiao.common.bean.type;

import androidx.annotation.NonNull;

public enum LogisticsCompany {
  // 中通，圆通，申通，韵达，极兔，顺丰，中国邮政，德邦
  ZHONG_TONG, YUAN_TONG, SHEN_TONG, YUN_DA, JI_TU, SHUN_FENG, CHINA_POST, DEPPON;

  @NonNull
  @Override
  public String toString() {
    switch (this) {
      case ZHONG_TONG:
        return "中通";
      case YUAN_TONG:
        return "圆通";
      case SHEN_TONG:
        return "申通";
      case YUN_DA:
        return "韵达";
      case JI_TU:
        return "极兔";
      case SHUN_FENG:
        return "顺丰";
      case CHINA_POST:
        return "中国邮政";
      case DEPPON:
        return "德邦";
      default:
        return "未知";
    }
  }

}
