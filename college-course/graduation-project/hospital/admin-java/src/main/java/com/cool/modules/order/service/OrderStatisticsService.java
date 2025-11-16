package com.cool.modules.order.service;

import com.cool.core.base.BaseService;
import com.cool.modules.order.dto.OrderStatisticsDto;
import com.cool.modules.order.entity.OrderStatisticsEntity;

/**
 * 订单统计Service
 */
public interface OrderStatisticsService extends BaseService<OrderStatisticsEntity> {

  /**
   * 订单统计
   * @param dto
   * @return ID
   */
  Long statistics(OrderStatisticsDto dto);

  /**
   * 统计本周订单
   * @return ID
   */
  Long statisticsThisWeek();

}