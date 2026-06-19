package com.ats.repository;

import com.ats.entity.RefreshToken;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface RefreshTokenMapper extends BaseMapper<RefreshToken> {

    /** 批量吊销某用户所有未过期 refresh token（登录时清理旧 token） */
    @Update("UPDATE refresh_tokens SET revoked = true WHERE user_id = #{userId} AND revoked = false AND expires_at > NOW()")
    int revokeAllByUserId(Long userId);
}
