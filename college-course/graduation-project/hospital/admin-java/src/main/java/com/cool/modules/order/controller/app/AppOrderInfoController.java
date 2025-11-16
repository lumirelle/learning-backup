package com.cool.modules.order.controller.app;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.cool.core.annotation.CoolRestController;
import com.cool.core.base.BaseController;
import com.cool.core.enums.QueryModeEnum;
import com.cool.core.request.CrudOption;
import com.cool.core.request.PageResult;
import com.cool.core.request.R;
import com.cool.core.util.CoolSecurityUtil;
import com.cool.core.util.EntityUtils;
import com.cool.modules.feedback.entity.ComplaintEntity;
import com.cool.modules.order.controller.app.params.WriteOffParam;
import com.cool.modules.order.entity.OrderInfoEntity;
import com.cool.modules.order.service.OrderInfoService;
import com.cool.modules.user.entity.UserInfoEntity;
import com.cool.modules.user.service.UserInfoService;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

import static com.cool.modules.accompany.entity.table.AccompanyStaffEntityTableDef.ACCOMPANY_STAFF_ENTITY;
import static com.cool.modules.order.entity.table.OrderInfoEntityTableDef.ORDER_INFO_ENTITY;
import static com.cool.modules.patient.entity.table.PatientInfoEntityTableDef.PATIENT_INFO_ENTITY;

/**
 * 订单信息
 */
@Tag(name = "订单信息", description = "订单信息")
@CoolRestController(api = {"page", "info", "update", "add"})
@RequiredArgsConstructor
public class AppOrderInfoController extends BaseController<OrderInfoService, OrderInfoEntity> {

    private final UserInfoService userInfoService;

    @Override
    protected void init(HttpServletRequest request, JSONObject requestParams) {
        Long userId = CoolSecurityUtil.getCurrentUserId();
        UserInfoEntity userInfoEntity = userInfoService.getById(userId);
        requestParams.set("userRole", userInfoEntity.getRole());

        // 设置分页查询条件
        setPageOption(createOp()
            .keyWordLikeFields(
                ORDER_INFO_ENTITY.ORDER_NUMBER,
                ORDER_INFO_ENTITY.REMARK
            )
            .fieldEq(
                ORDER_INFO_ENTITY.STATUS,
                ORDER_INFO_ENTITY.PATIENT_ID,
                ORDER_INFO_ENTITY.MEAL_ID,
                ORDER_INFO_ENTITY.PAY_TYPE
            )
            .queryWrapper(
                QueryWrapper.create()
                    .where(
                        PATIENT_INFO_ENTITY.PATIENT_USER_ID.eq(userId)
                            .or(ACCOMPANY_STAFF_ENTITY.STAFF_USER_ID.eq(userId))
                    )
            )
        );
    }

    @Operation(summary = "统计", description = "统计订单总数")
    @GetMapping("/personCount")
    public R<Long> personCount() {
        Long userId = CoolSecurityUtil.getCurrentUserId();
        return R.ok(service.countByUserId(userId));
    }

    @Operation(summary = "统计", description = "统计待付款订单总数")
    @GetMapping("/personCountWaitingPayment")
    public R<Long> personCountWaitingPayment() {
        Long userId = CoolSecurityUtil.getCurrentUserId();
        return R.ok(service.countWaitingPaymentByUserId(userId));
    }

    @Operation(summary = "统计", description = "统计待使用订单总数")
    @GetMapping("/personCountWaitingUse")
    public R<Long> personCountWaitingUse() {
        Long userId = CoolSecurityUtil.getCurrentUserId();
        return R.ok(service.countWaitingUseByUserId(userId));
    }

    @Operation(summary = "统计", description = "统计已完成订单总数")
    @GetMapping("/personCountComplete")
    public R<Long> personCountComplete() {
        Long userId = CoolSecurityUtil.getCurrentUserId();
        return R.ok(service.countCompleteByUserId(userId));
    }

    @Override
    @Operation(summary = "修改", description = "根据ID修改订单状态")
    @PostMapping("/update")
    public R update(@RequestBody OrderInfoEntity orderInfoEntity, @RequestAttribute() JSONObject requestParams) {
        Long id = orderInfoEntity.getId();
        JSONObject info = JSONUtil.parseObj(JSONUtil.toJsonStr(service.getById(id)));
        info.set("visitTime", orderInfoEntity.getVisitTime());
        info.set("remark", orderInfoEntity.getRemark());
        info.set("status", requestParams.get("status"));
        // 随机六位验证码
        info.set("verifyCode", RandomUtil.randomString("0123456789", 6));
        info.set("updateTime", new Date());
        info.set("payTime", new Date());
        service.update(requestParams, JSONUtil.toBean(info, currentEntityClass()));
        return R.ok();
    }

    @Override
    @Operation(summary = "信息", description = "根据ID查询单个信息")
    @GetMapping("/info")
    public R info(@RequestAttribute() JSONObject requestParams, @RequestParam() Long id, @RequestAttribute(COOL_INFO_OP) CrudOption<OrderInfoEntity> option) {
        OrderInfoEntity info = service.getById(id);
        String ignoreProperty = null;
        if (requestParams.containsKey("userRole") && requestParams.getInt("userRole") == 2) {
            ignoreProperty = "verifyCode";
        }
        return R.ok(EntityUtils.toMap(info, ignoreProperty));
    }

    @Operation(summary = "核销订单", description = "核销订单")
    @PostMapping("/writeOff")
    public R writeOff(@RequestBody WriteOffParam param) {
        Long id = param.getId();
        String verifyCode = param.getVerifyCode();
        try {
            service.writeOff(id, verifyCode);
        } catch (Exception e) {
            return R.error(e.getMessage());
        }
        return R.ok();
    }

} 
