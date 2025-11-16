package com.dorm.service.dorm.bill;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dorm.entity.dorm.bill.BillPO;
import com.dorm.mapper.dorm.bill.BillMapper;
import org.springframework.stereotype.Service;

@Service
public class BillServiceImpl extends ServiceImpl<BillMapper, BillPO> implements BillService {
}
