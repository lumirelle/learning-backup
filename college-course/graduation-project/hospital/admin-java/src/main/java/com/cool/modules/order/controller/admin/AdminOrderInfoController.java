package com.cool.modules.order.controller.admin;

import static com.cool.modules.order.entity.table.OrderInfoEntityTableDef.ORDER_INFO_ENTITY;

import cn.hutool.json.JSONUtil;
import com.cool.core.util.CoolSecurityUtil;
import org.springframework.web.bind.annotation.GetMapping;

import cn.hutool.json.JSONObject;
import com.cool.core.annotation.CoolRestController;
import com.cool.core.base.BaseController;
import com.cool.core.request.R;
import com.cool.modules.order.entity.OrderInfoEntity;
import com.cool.modules.order.service.OrderInfoService;
import com.mybatisflex.core.query.QueryWrapper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 订单信息管理
 */
@Tag(name = "订单信息管理", description = "管理订单信息")
@CoolRestController(api = {"update", "info", "page"})
public class AdminOrderInfoController extends BaseController<OrderInfoService, OrderInfoEntity> {

    // 如果传递了 payOrderOnly 参数，则仅分页展示支付的订单
    @Override
    protected void init(HttpServletRequest request, JSONObject requestParams) {

        boolean payOrderOnly = requestParams.getBool("payOrderOnly", false);

        setPageOption(
            createOp()
                .keyWordLikeFields(
                    ORDER_INFO_ENTITY.ORDER_NUMBER
                )
                // NOTE: UserId 是保留字段，请求时默认传递发起请求用户的 ID，因此本实体类中需要用 payUserId
                .fieldEq(
                    ORDER_INFO_ENTITY.ID,
                    ORDER_INFO_ENTITY.MEAL_ID,
                    ORDER_INFO_ENTITY.PATIENT_ID,
                    ORDER_INFO_ENTITY.STATUS,
                    ORDER_INFO_ENTITY.PAY_TYPE
                )
                .queryWrapper(QueryWrapper.create()
                    .where(ORDER_INFO_ENTITY.STATUS.in(1, 2, 3).when(payOrderOnly))
                )
        );
    }

    @Operation(summary = "统计", description = "统计付款订单总数")
    @GetMapping("/countPayed")
    public R<Long> countPayed() {
        return R.ok(service.countPayed());
    }

    @Operation(summary = "计算销售额", description = "计算销售额")
    @GetMapping("/sumAmount")
    public R<BigDecimal> sumAmount() {
        return R.ok(service.sumAmount());
    }

    @Override
    @PostMapping("/update")
    public R update(@RequestBody OrderInfoEntity orderInfoEntity, @RequestAttribute() JSONObject requestParams) {
        Long id = orderInfoEntity.getId();
        JSONObject info = JSONUtil.parseObj(JSONUtil.toJsonStr(service.getById(id)));
        requestParams.forEach(info::set);
        info.set("updateTime", new Date());
        info.set("remark", "支付备注：" + info.getStr("remark") + "（操作人员ID： " + CoolSecurityUtil.getCurrentUserId() + "）");
        service.update(requestParams, JSONUtil.toBean(info, currentEntityClass()));
        return R.ok();
    }
}
