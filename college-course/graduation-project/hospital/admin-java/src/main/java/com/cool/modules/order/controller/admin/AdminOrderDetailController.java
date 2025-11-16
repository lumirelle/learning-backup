package com.cool.modules.order.controller.admin;

import cn.hutool.json.JSONObject;
import com.cool.core.annotation.CoolRestController;
import com.cool.core.base.BaseController;
import com.cool.modules.order.entity.OrderDetailEntity;
import com.cool.modules.order.service.OrderDetailService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;

import static com.cool.modules.order.entity.table.OrderDetailEntityTableDef.ORDER_DETAIL_ENTITY;

/**
 * 订单详情管理
 */
@Tag(name = "订单详情管理", description = "管理订单详情")
@CoolRestController(api = {"update", "info", "page"})
public class AdminOrderDetailController extends
    BaseController<OrderDetailService, OrderDetailEntity> {

    @Override
    protected void init(HttpServletRequest request, JSONObject requestParams) {
        setPageOption(createOp()
            .fieldEq(
                ORDER_DETAIL_ENTITY.ORDER_ID,
                ORDER_DETAIL_ENTITY.ID,
                ORDER_DETAIL_ENTITY.AFTER_SALE_STATUS
            )
        );
    }

}
