package com.cool.modules.order.controller.app;

import cn.hutool.json.JSONObject;
import com.cool.core.annotation.CoolRestController;
import com.cool.core.base.BaseController;
import com.cool.modules.order.entity.OrderDetailEntity;
import com.cool.modules.order.entity.OrderInfoEntity;
import com.cool.modules.order.service.OrderDetailService;
import com.mybatisflex.core.query.QueryWrapper;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;

import static com.cool.modules.order.entity.table.OrderDetailEntityTableDef.ORDER_DETAIL_ENTITY;
import static com.cool.modules.order.entity.table.OrderInfoEntityTableDef.ORDER_INFO_ENTITY;

/**
 * 订单详情
 */
@Tag(name = "订单详情", description = "订单详情")
@CoolRestController(api = {"add", "page", "info"})
public class AppOrderDetailController extends BaseController<OrderDetailService, OrderDetailEntity> {

    @Override
    protected void init(HttpServletRequest request, JSONObject requestParams) {
        // 设置分页查询条件
        setPageOption(createOp()
            .fieldEq(
                ORDER_DETAIL_ENTITY.ORDER_ID,
                ORDER_DETAIL_ENTITY.AFTER_SALE_STATUS
            )
        );
    }
} 
