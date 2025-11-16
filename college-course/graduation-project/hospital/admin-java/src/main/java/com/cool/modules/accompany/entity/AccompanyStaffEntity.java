package com.cool.modules.accompany.entity;

import com.cool.core.base.BaseEntity;
import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Table;
import com.tangzc.mybatisflex.autotable.annotation.ColumnDefine;
import com.tangzc.mybatisflex.autotable.annotation.UniIndex;
import lombok.Getter;
import lombok.Setter;
import org.dromara.autotable.annotation.Index;

import java.util.Date;

/**
 * 陪诊员信息
 */
@Getter
@Setter
@Table(value = "accompany_staff", comment = "陪诊员信息")
public class AccompanyStaffEntity extends BaseEntity<AccompanyStaffEntity> {

    @ColumnDefine(comment = "姓名")
    private String name;

    @ColumnDefine(comment = "性别 0-未知 1-男 2-女", defaultValue = "0")
    private Integer gender;

    @ColumnDefine(comment = "生日")
    private Date birthday;

    @UniIndex
    @ColumnDefine(comment = "电话")
    private String phone;

    @ColumnDefine(comment = "级别 0-初级 1-中级 2-高级", defaultValue = "0")
    private Integer level;

    @ColumnDefine(comment = "状态 0-正常 1-请假 2-其他", defaultValue = "0")
    private Integer status;

    @ColumnDefine(comment = "简介")
    private String introduction;

    @ColumnDefine(comment = "备注")
    private String remark;

    @Index
    @ColumnDefine(comment = "关联用户ID")
    private Long staffUserId;

    @Column(ignore = true)
    private String nickName;

}
