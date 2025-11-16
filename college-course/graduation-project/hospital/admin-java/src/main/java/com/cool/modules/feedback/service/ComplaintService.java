package com.cool.modules.feedback.service;

import java.util.List;

import com.cool.core.base.BaseService;
import com.cool.modules.feedback.entity.ComplaintEntity;

/**
 * 投诉信息服务接口
 */
public interface ComplaintService extends BaseService<ComplaintEntity> {
  /**
   * 统计本年度每月的投诉数量
   */
  List<Long> countThisYear();
}