package com.dorm.entity.dorm;

import com.dorm.enums.dorm.DormStatus;
import com.dorm.utils.BeanConvertUtils;
import lombok.Data;
import lombok.NonNull;

import java.util.List;

@Data
public class DormVO {

    private Integer id;

    /**
     * 宿舍号，如：0709
     */
    private String no;

    /**
     * 宿舍楼，如：1 栋
     */
    private String building;

    private Integer setting;
    private Integer people;

    private DormStatus status;

    public static DormVO valueOf(
        @NonNull DormPO dormPO
    ) {
        return BeanConvertUtils.convert(DormVO.class, dormPO);
    }

    public static List<DormVO> valuesOf(
        @NonNull List<DormPO> dormPOList
    ) {
        return dormPOList.stream().map(DormVO::valueOf).toList();
    }
}
