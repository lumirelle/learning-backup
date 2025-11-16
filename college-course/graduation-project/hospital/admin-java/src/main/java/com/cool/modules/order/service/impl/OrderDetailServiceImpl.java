package com.cool.modules.order.service.impl;

import static com.cool.modules.order.entity.table.OrderDetailEntityTableDef.ORDER_DETAIL_ENTITY;

import com.cool.core.base.BaseServiceImpl;
import com.cool.modules.order.entity.OrderDetailEntity;
import com.cool.modules.order.mapper.OrderDetailMapper;
import com.cool.modules.order.service.OrderDetailService;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import org.springframework.stereotype.Service;

/**
 * 订单详情ServiceImpl
 */
@Service
public class OrderDetailServiceImpl extends BaseServiceImpl<OrderDetailMapper, OrderDetailEntity> implements
    OrderDetailService {
}
