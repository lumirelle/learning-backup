package com.dorm.service.dorm;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dorm.entity.dorm.BuildingSettingVO;
import com.dorm.entity.dorm.DormPO;

import java.util.List;
import java.util.Set;

public interface DormService extends IService<DormPO> {

    /**
     * 检查宿舍是否存在
     * @param building 宿舍楼
     * @param no 宿舍号
     * @return true：存在，false：不存在
     */
    boolean isDormExist(String building, String no);

    /**
     * 查询空闲（未住满）的宿舍
     * @return 空闲（未住满）的宿舍列表
     */
    List<DormPO> listFreeDorms();

    /**
     * 查询所有宿舍楼并去重
     * @return 所有宿舍楼（去重后）
     */
    Set<String> listUniqueBuildings();

    List<BuildingSettingVO> countPeoplePerBuild();

}
