package com.ats.repository;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * job_tags 复合主键 (job_id, tag_id) 不适配 MyBatis-Plus 单主键 BaseMapper，
 * 故 <strong>不继承</strong> BaseMapper，只暴露业务必要的两个原生方法。
 */
@Mapper
public interface JobTagMapper {

    /** 批量插入岗位-标签关联。created_at 走 PG 默认 NOW()。 */
    @Insert({
        "<script>",
        "INSERT INTO job_tags (job_id, tag_id) VALUES",
        "<foreach collection='tagIds' item='tagId' separator=','>(#{jobId}, #{tagId})</foreach>",
        "ON CONFLICT (job_id, tag_id) DO NOTHING",
        "</script>"
    })
    int batchInsert(@Param("jobId") Long jobId, @Param("tagIds") List<Long> tagIds);

    /** 删除指定岗位的所有标签关联（编辑岗位标签时全量替换用）。 */
    @Delete("DELETE FROM job_tags WHERE job_id = #{jobId}")
    int deleteByJobId(@Param("jobId") Long jobId);
}
