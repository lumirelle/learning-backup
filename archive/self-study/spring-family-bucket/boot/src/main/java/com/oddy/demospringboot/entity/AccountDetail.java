package com.oddy.demospringboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.sql.Date;
import lombok.Data;

@Schema(description = "账户资料实体类")
@Data
@Entity
@Table(name = "account_detail")
@TableName("account_detail")
public class AccountDetail {

  @Schema(description = "账户资料 id")
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  @Id
  @TableId(type = IdType.AUTO)
  private Integer id;

  @Schema(description = "账户创建时间")
  @Column(name = "created_date")
  @TableField("created_date")
  private Date createdDate;

}
