package com.ats.repository;

import com.ats.entity.Tag;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface TagMapper extends BaseMapper<Tag> {

    /**
     * 给定一组岗位 id，批量查出每个岗位对应的所有 tags。
     * 返回的 Tag 上会临时填充 jobId 字段（通过 alias）——但为了类型整洁，
     * 这里返回 JobTagRow 简单视图（避免污染 Tag 实体）。
     *
     * <p>注意：此方法返回 {@link JobTagRow} 而不是 List<Tag>，否则无法在 service 里按 jobId 分桶。
     */
    @Select({
        "<script>",
        "SELECT jt.job_id AS jobId, t.id, t.slug, t.name, t.category",
        "FROM job_tags jt",
        "JOIN tags t ON t.id = jt.tag_id",
        "WHERE jt.job_id IN",
        "<foreach collection='jobIds' item='id' open='(' separator=',' close=')'>#{id}</foreach>",
        "ORDER BY t.category, t.name",
        "</script>"
    })
    List<JobTagRow> findTagsByJobIds(@Param("jobIds") List<Long> jobIds);
}
