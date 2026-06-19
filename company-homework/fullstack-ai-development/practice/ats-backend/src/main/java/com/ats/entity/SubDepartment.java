package com.ats.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.OffsetDateTime;

/**
 * 组织树叶子节点（M6）。挂 HR、岗位、工作地点。
 * 必须挂在某个 department 下，不允许直挂 root，也不允许嵌套其他 sub_departments。
 */
@Data
@TableName(value = "sub_departments", autoResultMap = true)
public class SubDepartment {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long parentDepartmentId;
    private String name;
    /** 工作地点。原 jobs.location 字段下沉到此（M6）。 */
    private String location;

    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
