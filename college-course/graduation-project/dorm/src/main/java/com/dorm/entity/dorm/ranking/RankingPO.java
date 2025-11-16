package com.dorm.entity.dorm.ranking;

import com.baomidou.mybatisplus.annotation.TableName;
import com.dorm.entity.dorm.DormPO;
import com.dorm.utils.BeanConvertUtils;
import jakarta.validation.constraints.Null;
import lombok.Data;
import lombok.NonNull;
import org.apache.commons.math3.analysis.function.Add;

import java.util.Date;

@Data
@TableName("dorm_ranking")
public class RankingPO {

    private Integer id;

    private Integer dormId;

    /**
     * 卫生得分
     */
    private Integer healthScore;

    /**
     * 美观得分
     */
    private Integer beautyScore;

    /**
     * 安全得分
     */
    private Integer safetyScore;

    /**
     * 氛围（学习氛围、友善氛围）得分
     */
    private Integer atmosphereScore;

    private Date createTime;

    public static RankingPO valueOf(@NonNull AddRankingDTO dto) {
        return BeanConvertUtils.convert(RankingPO.class, dto);
    }

}
