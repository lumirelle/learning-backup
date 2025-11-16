package com.dorm.entity.dorm;

import com.baomidou.mybatisplus.annotation.TableName;
import com.dorm.enums.dorm.DormStatus;
import com.dorm.utils.BeanConvertUtils;
import lombok.Data;
import lombok.NonNull;

@Data
@TableName("dorm")
public class DormPO {

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

    public void increaseSetting() {
        this.setting++;
    }

    public void decreaseSetting() {
        this.setting--;
    }

    public static DormPO valueOf(@NonNull AddDormDTO addDormDTO) {
        return BeanConvertUtils.convert(DormPO.class, addDormDTO);
    }

}
