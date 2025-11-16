package com.cool.modules.user.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.crypto.digest.MD5;
import cn.hutool.json.JSONObject;
import com.cool.core.base.BaseServiceImpl;
import com.cool.core.util.EntityUtils;
import com.cool.modules.accompany.entity.AccompanyStaffEntity;
import com.cool.modules.accompany.mapper.AccompanyStaffMapper;
import com.cool.modules.patient.entity.PatientInfoEntity;
import com.cool.modules.patient.mapper.PatientInfoMapper;
import com.cool.modules.user.entity.UserInfoEntity;
import com.cool.modules.user.mapper.UserInfoMapper;
import com.cool.modules.user.service.UserInfoService;
import com.cool.modules.user.util.UserSmsUtil;
import com.cool.modules.user.util.UserSmsUtil.SendSceneEnum;
import com.mybatisflex.core.query.QueryWrapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

import static com.cool.modules.accompany.entity.table.AccompanyStaffEntityTableDef.ACCOMPANY_STAFF_ENTITY;
import static com.cool.modules.patient.entity.table.PatientInfoEntityTableDef.PATIENT_INFO_ENTITY;
import static com.cool.modules.user.entity.table.UserInfoEntityTableDef.USER_INFO_ENTITY;

@Service
@RequiredArgsConstructor
public class UserInfoServiceImpl extends BaseServiceImpl<UserInfoMapper, UserInfoEntity> implements
    UserInfoService {

    private final UserSmsUtil userSmsUtil;
    private final PatientInfoMapper patientInfoMapper;
    private final AccompanyStaffMapper accompanyStaffMapper;

    @Override
    public UserInfoEntity person(Long userId) {
        UserInfoEntity info = getById(userId);
        info.setPassword(null);
        return info;
    }

    @Override
    public Map<String, Object> profile(Long userId) {
        UserInfoEntity info = getById(userId);
        if (info.getRole() == 1) {
            return EntityUtils.toMap(patientInfoMapper.selectOneByQuery(
                QueryWrapper.create()
                    .where(PATIENT_INFO_ENTITY.PATIENT_USER_ID.eq(userId))
            ));
        } else if (info.getRole() == 2) {
            return EntityUtils.toMap(accompanyStaffMapper.selectOneByQuery(
                QueryWrapper.create()
                    .where(ACCOMPANY_STAFF_ENTITY.STAFF_USER_ID.eq(userId))
            ));
        }
        return Map.of();
    }

    @Override
    public boolean addProfile(Long userId, JSONObject params) {
        Integer role = params.get("role", Integer.class);
        UserInfoEntity info = getById(userId);
        if (role == 1) {
            info.setRole(role);
            mapper.update(info);

            PatientInfoEntity profile = params.toBean(PatientInfoEntity.class);
            profile.setPatientUserId(userId);
            patientInfoMapper.insert(profile);
            return true;
        } else if (role == 2) {
            info.setRole(role);
            mapper.update(info);

            AccompanyStaffEntity profile = params.toBean(AccompanyStaffEntity.class);
            profile.setLevel(0);
            profile.setStaffUserId(userId);
            accompanyStaffMapper.insert(profile);
            return true;
        }
        return false;
    }

    @Override
    public boolean updateProfile(Long userId, JSONObject params) {
        UserInfoEntity info = getById(userId);
        if (info.getRole() == 1) {
            patientInfoMapper.updateByQuery(
                params.toBean(PatientInfoEntity.class),
                QueryWrapper.create()
                    .where(PATIENT_INFO_ENTITY.PATIENT_USER_ID.eq(userId))
            );
            return true;
        } else if (info.getRole() == 2) {
            accompanyStaffMapper.updateByQuery(
                params.toBean(AccompanyStaffEntity.class),
                QueryWrapper.create()
                    .where(ACCOMPANY_STAFF_ENTITY.STAFF_USER_ID.eq(userId))
            );
            return true;
        }
        return false;
    }

    @Override
    public void updatePassword(Long userId, String password, String code) {
        UserInfoEntity info = getById(userId);
        userSmsUtil.checkVerifyCode(info.getPhone(), code, SendSceneEnum.ALL);
        info.setPassword(MD5.create().digestHex(password));
        info.updateById();
    }

    @Override
    public void logoff(Long userId) {
        UserInfoEntity info = new UserInfoEntity();
        info.setId(userId);
        info.setStatus(2);
        info.setNickName("已注销-00" + userId);
        info.updateById();
    }

    @Override
    public void bindPhone(Long userId, String phone, String code) {
        userSmsUtil.checkVerifyCode(phone, code, SendSceneEnum.ALL);
        UserInfoEntity info = new UserInfoEntity();
        info.setId(userId);
        info.setPhone(phone);
        info.updateById();
    }

    @Override
    public void bindRole(Long userId, Integer role) {
        UserInfoEntity info = new UserInfoEntity();
        info.setId(userId);
        info.setRole(role);
        info.updateById();
    }

    @Override
    public Long countToday() {
        return count(QueryWrapper.create().from(USER_INFO_ENTITY).where(USER_INFO_ENTITY.CREATE_TIME.ge(DateUtil.offsetDay(DateUtil.date(), -1))));
    }
}
