package com.dorm.entity.card.bandwidth;

import com.dorm.entity.card.telephone.TelephoneCardPO;
import com.dorm.enums.card.telephone.TelephoneCardOperator;
import com.dorm.enums.card.telephone.TelephoneCardStatus;
import com.dorm.utils.BeanConvertUtils;
import lombok.Data;
import lombok.NonNull;

@Data
public class BandwidthVO {
    // from BandwidthPO
    private Integer id;
    private Integer speed;
    private Integer telephoneCardId;

    // from TelephoneCardPO
    private String telephone;
    private TelephoneCardOperator operator;
    private TelephoneCardStatus status;

    public static BandwidthVO valueOf(@NonNull BandwidthPO bandwidthPO, TelephoneCardPO telephoneCardPO) {
        return BeanConvertUtils.convert(BandwidthVO.class, bandwidthPO, telephoneCardPO);
    }

}
