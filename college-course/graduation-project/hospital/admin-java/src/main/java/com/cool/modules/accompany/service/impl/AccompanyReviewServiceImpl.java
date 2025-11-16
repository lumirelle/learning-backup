package com.cool.modules.accompany.service.impl;

import cn.hutool.json.JSONObject;
import com.cool.core.base.BaseServiceImpl;
import com.cool.modules.accompany.entity.AccompanyReviewEntity;
import com.cool.modules.accompany.mapper.AccompanyReviewMapper;
import com.cool.modules.accompany.service.AccompanyReviewService;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import org.springframework.stereotype.Service;

import static com.cool.modules.accompany.entity.table.AccompanyReviewEntityTableDef.ACCOMPANY_REVIEW_ENTITY;
import static com.cool.modules.accompany.entity.table.AccompanyStaffEntityTableDef.ACCOMPANY_STAFF_ENTITY;

@Service
public class AccompanyReviewServiceImpl extends BaseServiceImpl<AccompanyReviewMapper, AccompanyReviewEntity>
    implements AccompanyReviewService {

    @Override
    public Object page(JSONObject requestParams, Page<AccompanyReviewEntity> page, QueryWrapper queryWrapper) {
        queryWrapper.select(
                ACCOMPANY_REVIEW_ENTITY.ALL_COLUMNS,
                ACCOMPANY_STAFF_ENTITY.NAME.as("staffName")
            )
            .from(ACCOMPANY_REVIEW_ENTITY)
            .leftJoin(ACCOMPANY_STAFF_ENTITY).on(ACCOMPANY_REVIEW_ENTITY.STAFF_ID.eq(ACCOMPANY_STAFF_ENTITY.ID));
        return mapper.paginateWithRelations(page, queryWrapper);
    }

    @Override
    public Object info(Long id) {
        QueryWrapper queryWrapper = QueryWrapper.create()
            .select(
                ACCOMPANY_REVIEW_ENTITY.ALL_COLUMNS,
                ACCOMPANY_STAFF_ENTITY.NAME.as("staffName")
            )
            .from(ACCOMPANY_REVIEW_ENTITY)
            .leftJoin(ACCOMPANY_STAFF_ENTITY).on(ACCOMPANY_REVIEW_ENTITY.STAFF_ID.eq(ACCOMPANY_STAFF_ENTITY.ID))
            .where(ACCOMPANY_REVIEW_ENTITY.ID.eq(id));
        return mapper.selectOneWithRelationsByQuery(queryWrapper);
    }
}
