package com.ats.repository;

import com.ats.entity.InterviewRecord;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface InterviewMapper extends BaseMapper<InterviewRecord> {

    /**
     * 列出某 application 的全部面试记录，含面试官姓名/角色（用于详情时间线）。
     * <p>按 created_at 升序，让最早的轮次排前面。</p>
     */
    @Select({
            "SELECT ir.id, ir.application_id, ir.interviewer_id,",
            "       u.full_name AS interviewer_name, u.role AS interviewer_role,",
            "       ir.round, ir.rating, ir.strengths, ir.weaknesses,",
            "       ir.conclusion, ir.notes, ir.created_at, ir.updated_at",
            "FROM interview_records ir",
            "LEFT JOIN users u ON u.id = ir.interviewer_id",
            "WHERE ir.application_id = #{applicationId}",
            "ORDER BY ir.created_at ASC, ir.id ASC"
    })
    List<Map<String, Object>> listByApplication(@Param("applicationId") Long applicationId);
}
