package com.dorm.service.card.all_in_one.bill;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.dorm.entity.card.all_in_one.bill.AllInOneCardBillPO;

import java.util.List;

public interface AllInOneCardBillService extends IService<AllInOneCardBillPO> {

    List<AllInOneCardBillPO> listByCreateTimeDesc();

    List<AllInOneCardBillPO> listByCreateTimeDesc(QueryWrapper<AllInOneCardBillPO> wrapper);

}
