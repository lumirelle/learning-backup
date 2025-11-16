package com.cool.modules.accompany.service;

import cn.hutool.json.JSONObject;
import com.cool.core.base.BaseService;
import com.cool.modules.accompany.entity.AccompanyReviewEntity;
import com.cool.modules.accompany.entity.AccompanyStaffEntity;
import org.springframework.web.bind.annotation.RequestParam;

public interface AccompanyStaffService extends BaseService<AccompanyStaffEntity> {

    void doReview(JSONObject requestParam, AccompanyReviewEntity reviewEntity);

}