package com.dorm.service.card.all_in_one;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dorm.mapper.card.all_in_one.AllInOneCardMapper;
import com.dorm.entity.card.all_in_one.AllInOneCardPO;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;

@Service
public class AllInOneCardServiceImpl extends ServiceImpl<AllInOneCardMapper, AllInOneCardPO> implements AllInOneCardService {
}
