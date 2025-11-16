package com.dorm.service.dorm.ranking;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dorm.entity.dorm.ranking.RankingPO;
import com.dorm.mapper.dorm.ranking.RankingMapper;
import org.springframework.stereotype.Service;

@Service
public class RankingServiceImpl extends ServiceImpl<RankingMapper, RankingPO> implements RankingService {
}
