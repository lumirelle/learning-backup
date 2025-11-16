package com.cool.modules.order.service;

import com.cool.core.base.BaseService;
import com.cool.modules.order.entity.OrderInfoEntity;

import java.math.BigDecimal;

/**
 * 订单信息Service
 */
public interface OrderInfoService extends BaseService<OrderInfoEntity> {

  Long countByUserId(Long userId);

  Long countWaitingPaymentByUserId(Long userId);

  Long countWaitingUseByUserId(Long userId);

  Long countCompleteByUserId(Long userId);

  /**
   * 统计付款订单总数
   */
  Long countPayed();

  BigDecimal sumAmount();

  void writeOff(Long id, String verifyCode);

}
