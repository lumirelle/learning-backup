package com.cool.modules.feedback.entity;

import com.cool.core.base.BaseEntity;
import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Table;
import com.mybatisflex.core.handler.Fastjson2TypeHandler;
import com.tangzc.mybatisflex.autotable.annotation.ColumnDefine;
import com.tangzc.mybatisflex.autotable.annotation.UniIndex;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 投诉信息
 */
@Getter
@Setter
@Table(value = "feedback_complaint", comment = "投诉信息")
public class ComplaintEntity extends BaseEntity<ComplaintEntity> {

    // Order
    @UniIndex
    @ColumnDefine(comment = "订单 ID")
    private String orderId;

    @Column(ignore = true)
    private String orderNumber;

    @ColumnDefine(comment = "类型 0=服务态度 1=价格问题 2=服务质量 3=其他", defaultValue = "0")
    private Integer type;

    @ColumnDefine(comment = "状态 0=待处理 1=处理中 2=已解决 3=未解决 4=已关闭", defaultValue = "0")
    private Integer status;

    @ColumnDefine(comment = "内容", type = "TEXT")
    private String content;

    @ColumnDefine(comment = "联系方式")
    private String contactInfo;

    @ColumnDefine(comment = "图片", type = "json")
    @Column(typeHandler = Fastjson2TypeHandler.class)
    private List<String> images;

    // USER
    @ColumnDefine(comment = "用户ID")
    private Long complaintUserId;

    @Column(ignore = true)
    private String userNickName;

    // SYS_USER
    @ColumnDefine(comment = "处理人ID")
    private Long handlerId;

    @Column(ignore = true)
    private String handlerNickName;

    @ColumnDefine(comment = "处理结果")
    private String handleResult;

    @ColumnDefine(comment = "备注")
    private String remark;

}
