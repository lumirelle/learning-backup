package org.seagulls.cainiaoserver.enums;

public enum LogisticsCompany {
  // 中通，圆通，申通，韵达，极兔，顺丰，中国邮政，德邦
  ZHONG_TONG, YUAN_TONG, SHEN_TONG, YUN_DA, JI_TU, SHUN_FENG, CHINA_POST, DEPPON;

  @Override
  public String toString() {
    return switch (this) {
      case ZHONG_TONG -> "中通";
      case YUAN_TONG -> "圆通";
      case SHEN_TONG -> "申通";
      case YUN_DA -> "韵达";
      case JI_TU -> "极兔";
      case SHUN_FENG -> "顺丰";
      case CHINA_POST -> "中国邮政";
      case DEPPON -> "德邦";
    };
  }

  public String getPrefix() {
    return switch (this) {
      case ZHONG_TONG -> "ZT";
      case YUAN_TONG -> "YT";
      case SHEN_TONG -> "ST";
      case YUN_DA -> "YD";
      case JI_TU -> "JT";
      case SHUN_FENG -> "SF";
      case CHINA_POST -> "CP";
      case DEPPON -> "DP";
    };
  }
}
