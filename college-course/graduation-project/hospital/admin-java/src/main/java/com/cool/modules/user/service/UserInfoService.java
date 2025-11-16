package com.cool.modules.user.service;

import cn.hutool.json.JSONObject;
import com.cool.core.base.BaseService;
import com.cool.modules.user.entity.UserInfoEntity;

import java.util.Map;

public interface UserInfoService extends BaseService<UserInfoEntity> {
    /**
     * 用户个人信息
     *
     * @param userId
     * @return
     */
    UserInfoEntity person(Long userId);

    Map<String, Object> profile(Long userId);

    boolean addProfile(Long userId, JSONObject params);

    boolean updateProfile(Long userId, JSONObject params);

    /**
     * 更新用户密码
     */
    void updatePassword(Long userId, String password, String code);

    /**
     * 注销
     */
    void logoff(Long currentUserId);

    /**
     * 绑定手机号
     */
    void bindPhone(Long currentUserId, String phone, String code);

    void bindRole(Long currentUserId, Integer role);

    /**
     * 统计日增总量
     */
    Long countToday();

}
