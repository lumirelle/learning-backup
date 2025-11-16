package com.cool.modules.accompany.service.impl;

import cn.hutool.json.JSONObject;
import com.cool.core.base.BaseServiceImpl;
import com.cool.modules.accompany.entity.AccompanyReviewEntity;
import com.cool.modules.accompany.entity.AccompanyStaffEntity;
import com.cool.modules.accompany.mapper.AccompanyReviewMapper;
import com.cool.modules.accompany.mapper.AccompanyStaffMapper;
import com.cool.modules.accompany.service.AccompanyStaffService;
import com.cool.modules.user.entity.UserInfoEntity;
import com.cool.modules.user.entity.table.UserInfoEntityTableDef;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.update.UpdateChain;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static com.cool.modules.accompany.entity.table.AccompanyStaffEntityTableDef.ACCOMPANY_STAFF_ENTITY;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccompanyStaffServiceImpl extends BaseServiceImpl<AccompanyStaffMapper, AccompanyStaffEntity> implements AccompanyStaffService {

    private final AccompanyReviewMapper reviewMapper;

    @Override
    public Object page(JSONObject requestParams, Page<AccompanyStaffEntity> page, QueryWrapper queryWrapper) {
        queryWrapper
            .select(
                ACCOMPANY_STAFF_ENTITY.ALL_COLUMNS,
                UserInfoEntityTableDef.USER_INFO_ENTITY.NICK_NAME.as("nickName")
            )
            .from(ACCOMPANY_STAFF_ENTITY)
            .leftJoin(UserInfoEntityTableDef.USER_INFO_ENTITY)
            .on(AccompanyStaffEntity::getStaffUserId, UserInfoEntity::getId);
        return mapper.paginateWithRelations(page, queryWrapper);
    }

    @Override
    public void doReview(JSONObject requestParams, AccompanyReviewEntity reviewEntity) {
        // 保存审核记录
        reviewMapper.insert(reviewEntity);

        // 更新陪诊员信息
        UpdateChain.create(mapper)
            .set(ACCOMPANY_STAFF_ENTITY.LEVEL, requestParams.getStr("level"))
            .where(ACCOMPANY_STAFF_ENTITY.ID.eq(requestParams.getLong("staffId")))
            .update();
    }
}
