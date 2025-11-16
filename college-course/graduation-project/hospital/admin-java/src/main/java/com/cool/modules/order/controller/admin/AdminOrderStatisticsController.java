package com.cool.modules.order.controller.admin;

import cn.hutool.core.lang.Dict;
import cn.hutool.json.JSONObject;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;

import com.cool.core.annotation.CoolRestController;
import com.cool.core.base.BaseController;
import com.cool.core.request.R;
import com.cool.modules.order.dto.OrderStatisticsDto;
import com.cool.modules.order.entity.OrderStatisticsEntity;
import com.cool.modules.order.service.OrderStatisticsService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;

/**
 * 订单统计管理
 */
@Tag(name = "订单统计管理", description = "管理订单统计")
@CoolRestController(api = {"add", "info", "page"})
public class AdminOrderStatisticsController extends
    BaseController<OrderStatisticsService, OrderStatisticsEntity> {

    @Override
    protected void init(HttpServletRequest request, JSONObject requestParams) {
    }

    @Override
    @PostMapping("/add")
    @Operation(summary = "订单统计", description = "生成订单统计")
    public R add(@RequestAttribute() JSONObject requestParams) {
        return R.ok(Dict.create()
            .set("id", service.statistics(requestParams.toBean(OrderStatisticsDto.class)))
        );
    }

    @PostMapping("/addThisWeek")
    @Operation(summary = "本周订单统计", description = "生成本周订单统计")
    public R addThisWeek() {
        return R.ok(Dict.create()
            .set("id", service.statisticsThisWeek())
        );
    }

}
