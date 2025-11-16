package com.cool.modules.accompany.entity;

import com.cool.core.base.BaseEntity;
import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Table;
import com.tangzc.mybatisflex.autotable.annotation.ColumnDefine;
import lombok.Getter;
import lombok.Setter;
import org.dromara.autotable.annotation.Index;

@Getter
@Setter
@Table(value = "accompany_staff_review", comment = "陪诊员资质审核信息")
public class AccompanyReviewEntity extends BaseEntity<AccompanyReviewEntity> {

    @Index
    @ColumnDefine(comment = "陪诊员 ID")
    private Long staffId;

    @Column(ignore = true)
    private String staffName;

    @ColumnDefine(comment = "审核员原级别 0-初级 1-中级 2-高级")
    private Integer oldLevel;

    @ColumnDefine(comment = "审核员新级别 0-初级 1-中级 2-高级")
    private Integer level;

    @ColumnDefine(comment = "审核意见")
    private String remark;

}
