package com.cool.modules.feedback.controller.app;

import cn.hutool.core.lang.Dict;
import cn.hutool.json.JSONObject;
import com.cool.core.annotation.CoolRestController;
import com.cool.core.base.BaseController;
import com.cool.core.request.R;
import com.cool.core.util.CoolSecurityUtil;
import com.cool.modules.accompany.entity.AccompanyStaffEntity;
import com.cool.modules.accompany.service.AccompanyStaffService;
import com.cool.modules.feedback.entity.ComplaintEntity;
import com.cool.modules.feedback.service.ComplaintService;
import com.cool.modules.user.entity.UserInfoEntity;
import com.cool.modules.base.entity.sys.BaseSysUserEntity;
import com.cool.modules.user.service.UserInfoService;
import com.mybatisflex.core.query.QueryWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;

import static com.cool.modules.accompany.entity.table.AccompanyStaffEntityTableDef.ACCOMPANY_STAFF_ENTITY;
import static com.cool.modules.feedback.entity.table.ComplaintEntityTableDef.COMPLAINT_ENTITY;
import static com.cool.modules.user.entity.table.UserInfoEntityTableDef.USER_INFO_ENTITY;
import static com.cool.modules.base.entity.sys.table.BaseSysUserEntityTableDef.BASE_SYS_USER_ENTITY;

/**
 * 投诉信息
 */
@Tag(name = "投诉信息", description = "投诉信息")
@CoolRestController(api = {"page", "add", "info"})
@RequiredArgsConstructor
public class AppComplaintController extends BaseController<ComplaintService, ComplaintEntity> {

    private final AccompanyStaffService accompanyStaffService;

    @Override
    protected void init(HttpServletRequest request, JSONObject requestParams) {
        Long userId = CoolSecurityUtil.getCurrentUserId();
        AccompanyStaffEntity accompanyStaffEntity = accompanyStaffService.getOne(
            QueryWrapper.create()
                .where(ACCOMPANY_STAFF_ENTITY.STAFF_USER_ID.eq(userId))
        );
        requestParams.set("staffId", accompanyStaffEntity.getId());

        // 设置分页查询条件
        setPageOption(createOp()
            .keyWordLikeFields(
                COMPLAINT_ENTITY.CONTENT,
                COMPLAINT_ENTITY.HANDLE_RESULT,
                COMPLAINT_ENTITY.REMARK
            )
            .fieldEq(
                COMPLAINT_ENTITY.TYPE,
                COMPLAINT_ENTITY.STATUS,
                COMPLAINT_ENTITY.COMPLAINT_USER_ID,
                COMPLAINT_ENTITY.ORDER_ID
            )
        );
    }

    @Override
    @Operation(summary = "新增", description = "新增信息，对应后端的实体类")
    @PostMapping("/add")
    protected R add(@RequestAttribute() JSONObject requestParams) {
        ComplaintEntity entity = requestParams.toBean(currentEntityClass());
        entity.setComplaintUserId(CoolSecurityUtil.getCurrentUserId());
        return R.ok(Dict.create().set("id", service.add(requestParams, entity)));
    }

}
