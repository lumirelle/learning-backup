package com.dorm.entity.card.bandwidth;

import com.baomidou.mybatisplus.annotation.TableName;
import com.dorm.utils.BeanConvertUtils;
import lombok.Data;
import lombok.NonNull;

@Data
@TableName("card_bandwidth")
public class BandwidthPO {

    private Integer id;

    private Integer speed;

    private Integer telephoneCardId;

    public static BandwidthPO valueOf(@NonNull AddBandwidthDTO addBandwidthDTO) {
        return BeanConvertUtils.convert(BandwidthPO.class, addBandwidthDTO);
    }

}
