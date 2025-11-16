package com.dorm.service.card.bandwidth;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dorm.entity.card.bandwidth.BandwidthPO;
import com.dorm.entity.card.telephone.TelephoneCardPO;
import com.dorm.mapper.card.bandwidth.BandwidthMapper;
import com.dorm.mapper.card.telephone.TelephoneCardMapper;
import org.springframework.stereotype.Service;

@Service
public class BandwidthServiceImpl extends ServiceImpl<BandwidthMapper, BandwidthPO> implements BandwidthService {
}
