package com.dorm.service.card.telephone;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dorm.entity.card.telephone.TelephoneCardPO;

import java.util.List;

public interface TelephoneCardService extends IService<TelephoneCardPO> {

    List<TelephoneCardPO> listNotCanceled();

}
