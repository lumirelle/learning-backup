package com.dorm.mapper.dorm;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dorm.entity.dorm.BuildingSettingVO;
import com.dorm.entity.dorm.DormPO;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DormMapper extends BaseMapper<DormPO> {

    List<BuildingSettingVO> listSettingPerBuilding();

}
