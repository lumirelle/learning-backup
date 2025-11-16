package com.dorm.entity.dorm.ranking;

import com.dorm.entity.dorm.DormPO;
import com.dorm.utils.BeanConvertUtils;
import lombok.Data;
import lombok.NonNull;

import java.util.Date;

@Data
public class RankingVO {

    // from RankingPO
    private Integer id;
    private Integer healthScore;
    private Integer beautyScore;
    private Integer safetyScore;
    private Integer atmosphereScore;
    private Integer totalScore;
    private Date createTime;
    private Integer dormId;

    // from DormPO
    private String dorm;

    public static RankingVO valueOf(@NonNull RankingPO rankingPO, DormPO dormPO) {
        RankingVO rankingVO = BeanConvertUtils.convert(RankingVO.class, rankingPO);
        // 计算总分
        rankingVO.setTotalScore(rankingVO.getHealthScore() + rankingVO.getBeautyScore() + rankingVO.getSafetyScore() + rankingVO.getAtmosphereScore());
        if (dormPO != null) {
            rankingVO.setDorm(dormPO.getBuilding() + ' ' + dormPO.getNo());
        }
        return rankingVO;
    }

}
