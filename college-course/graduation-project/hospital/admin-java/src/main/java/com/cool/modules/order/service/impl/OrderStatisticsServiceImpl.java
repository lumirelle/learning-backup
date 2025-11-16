package com.cool.modules.order.service.impl;

import com.cool.core.base.BaseServiceImpl;
import com.cool.modules.order.dto.OrderStatisticsDto;
import com.cool.modules.order.entity.OrderInfoEntity;
import com.cool.modules.order.entity.OrderStatisticsEntity;
import com.cool.modules.order.mapper.OrderInfoMapper;
import com.cool.modules.order.mapper.OrderStatisticsMapper;
import com.cool.modules.order.service.OrderStatisticsService;
import com.mybatisflex.core.query.QueryWrapper;

import cn.hutool.core.date.DateUtil;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Service;

import static com.cool.modules.order.entity.table.OrderStatisticsEntityTableDef.ORDER_STATISTICS_ENTITY;

/**
 * 订单统计ServiceImpl
 */
@Service
@AllArgsConstructor
public class OrderStatisticsServiceImpl extends
    BaseServiceImpl<OrderStatisticsMapper, OrderStatisticsEntity> implements OrderStatisticsService {

    private final OrderInfoMapper orderInfoMapper;

    @Override
    public Long statistics(OrderStatisticsDto dto) {
        Date startDate = dto.getStartDate();
        Date endDate = dto.getEndDate();

        // 查询订单统计数据
        List<OrderInfoEntity> orderInfoEntities = orderInfoMapper.selectListByQuery(QueryWrapper.create(new OrderInfoEntity())
            .between(OrderInfoEntity::getCreateTime, startDate, endDate));

        // 计算订单统计数据
        OrderStatisticsEntity result = new OrderStatisticsEntity();
        result.setStartDate(startDate);
        result.setEndDate(endDate);
        result.setTotalOrders(orderInfoEntities.size());
        result.setTotalAmount(orderInfoEntities.stream().map(OrderInfoEntity::getTotalAmount).reduce(BigDecimal.ZERO, BigDecimal::add));
        result.setTotalActualAmount(orderInfoEntities.stream().map(OrderInfoEntity::getActualAmount).reduce(BigDecimal.ZERO, BigDecimal::add));

        // 计算退款数
        result.setRefundCount(orderInfoEntities.stream().filter(i -> i.getStatus() == 5).count());
        // 计算完成数
        result.setCompletedCount(orderInfoEntities.stream().filter(i -> i.getStatus() == 3).count());
        // 计算取消数
        result.setCancelledCount(orderInfoEntities.stream().filter(i -> i.getStatus() == 4).count());
        // 计算支付订单数
        result.setPaidOrders(orderInfoEntities.stream().filter(i -> i.getStatus() == 2).count());

        // 保存订单统计数据
        mapper.insert(result);

        return result.getId();
    }

    @Override
    public Long statisticsThisWeek() {
        // 获取本周一和周日
        Date startDate = DateUtil.beginOfWeek(DateUtil.date());
        Date endDate = DateUtil.endOfWeek(DateUtil.date());

        // 如果已经统计过，则不进行统计
        OrderStatisticsEntity orderStatisticsEntity = mapper.selectOneByQuery(
            QueryWrapper.create()
                .where(ORDER_STATISTICS_ENTITY.START_DATE.eq(startDate))
                .and(ORDER_STATISTICS_ENTITY.END_DATE.eq(endDate))
        );
        if (orderStatisticsEntity != null) {
            return null;
        }

        // 如果未统计过，则进行统计
        OrderStatisticsDto dto = new OrderStatisticsDto(startDate, endDate);
        return statistics(dto);
    }

}