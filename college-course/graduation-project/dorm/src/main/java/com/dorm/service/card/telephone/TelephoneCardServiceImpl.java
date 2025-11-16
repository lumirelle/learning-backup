package com.dorm.service.card.telephone;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dorm.entity.card.telephone.TelephoneCardPO;
import com.dorm.enums.card.telephone.TelephoneCardStatus;
import com.dorm.mapper.card.telephone.TelephoneCardMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TelephoneCardServiceImpl extends ServiceImpl<TelephoneCardMapper, TelephoneCardPO> implements TelephoneCardService {
    @Override
    public List<TelephoneCardPO> listNotCanceled() {
        return getBaseMapper().selectList(new QueryWrapper<TelephoneCardPO>().ne("status", TelephoneCardStatus.CANCEL));
    }
}
