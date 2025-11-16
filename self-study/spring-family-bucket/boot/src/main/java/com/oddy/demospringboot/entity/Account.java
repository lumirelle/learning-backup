package com.oddy.demospringboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Data;

// 实体类本身及其属性使用 @Schema （架构）注解来添加描述
@Schema(description = "账户实体类")
@Data
@Entity // JPA 实体类注解
@Table(name = "account") // 对应的表
@TableName("account") // MyBatis Plus 实体类注解（对应的表）
public class Account {

  @Schema(description = "账户 id")
  @GeneratedValue(strategy = GenerationType.IDENTITY) // JPA 生成值策略：自增
  @Column(name = "id") // 对应的列
  @Id // 标记为主键
  @TableId(type = IdType.AUTO) // MP 主键注解，设置主键类型为自增主键
  private Integer id;

  @Schema(description = "账户名称")
  @Column(name = "name", nullable = false) // JPA 字段注解
  @TableField("name") // MP 字段注解
  private String name;

  @Schema(description = "账户使用者的性别")
  @Column(name = "sex")
  @TableField("sex")
  private String sex;

  @Schema(description = "账户使用者的年龄")
  @Column(name = "age")
  @TableField("age")
  private Integer age;

  @Schema(description = "账户使用者的邮件地址")
  @Column(name = "email", nullable = false)
  @TableField("email")
  private String email;

  @Schema(description = "账户密码")
  @Column(name = "password", nullable = false)
  @TableField("password")
  private String password;

  // JPA 关联查询
  @Schema(description = "账户详细资料")
  @JoinColumn(name = "detail_id") // 外键字段名称
  @OneToOne(cascade = CascadeType.ALL) // 外键关系，设置级联（关联）操作，设置之后如果有关联的数据，会一同进行操作
  // @OneToOne(fetch = FetchType.LAZY) 懒获取模式需要在事务模式下生效
  @TableField("detail_id")
  private AccountDetail accountDetail;

}
