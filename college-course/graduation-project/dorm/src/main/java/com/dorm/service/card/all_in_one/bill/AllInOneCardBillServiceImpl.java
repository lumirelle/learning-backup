package com.dorm.service.card.all_in_one.bill;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dorm.entity.card.all_in_one.AllInOneCardPO;
import com.dorm.entity.card.all_in_one.bill.AllInOneCardBillPO;
import com.dorm.entity.card.all_in_one.bill.AllInOneCardBillVO;
import com.dorm.mapper.card.all_in_one.AllInOneCardMapper;
import com.dorm.mapper.card.all_in_one.bill.AllInOneCardBillMapper;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Service
public class AllInOneCardBillServiceImpl extends ServiceImpl<AllInOneCardBillMapper, AllInOneCardBillPO> implements AllInOneCardBillService {

    @Override
    public List<AllInOneCardBillPO> listByCreateTimeDesc() {
        return listByCreateTimeDesc(new QueryWrapper<>());
    }

    @Override
    public List<AllInOneCardBillPO> listByCreateTimeDesc(QueryWrapper<AllInOneCardBillPO> wrapper) {
        return list(wrapper.orderByDesc("create_time"));
    }
}
