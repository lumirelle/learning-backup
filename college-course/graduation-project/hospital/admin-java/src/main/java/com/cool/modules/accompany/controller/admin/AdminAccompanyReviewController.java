package com.cool.modules.accompany.controller.admin;

import cn.hutool.json.JSONObject;
import com.cool.core.annotation.CoolRestController;
import com.cool.core.base.BaseController;
import com.cool.modules.accompany.entity.AccompanyReviewEntity;
import com.cool.modules.accompany.service.AccompanyReviewService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;

import static com.cool.modules.accompany.entity.table.AccompanyReviewEntityTableDef.ACCOMPANY_REVIEW_ENTITY;

/**
 * 陪诊员审核记录信息
 */
@Tag(name = "陪诊员审核记录管理", description = "陪诊员审核记录信息管理")
@CoolRestController(api = {"info", "page"})
@AllArgsConstructor
public class AdminAccompanyReviewController extends BaseController<AccompanyReviewService, AccompanyReviewEntity> {

    @Override
    protected void init(HttpServletRequest request, JSONObject requestParams) {
        setPageOption(
            createOp()
                .keyWordLikeFields(
                    ACCOMPANY_REVIEW_ENTITY.REMARK
                )
                .fieldEq(
                    ACCOMPANY_REVIEW_ENTITY.ID,
                    ACCOMPANY_REVIEW_ENTITY.STAFF_ID
                )
        );
    }

}
