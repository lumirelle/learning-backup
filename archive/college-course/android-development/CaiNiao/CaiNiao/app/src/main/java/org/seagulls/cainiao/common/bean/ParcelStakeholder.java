package org.seagulls.cainiao.common.bean;

import java.io.Serializable;
import lombok.Data;

/**
 * 包裹干系人数据类，即“寄件人”和“收件人”
 */
@Data
public class ParcelStakeholder implements Serializable {

  private static final long serialVersionUID = 1L;

  private int id;

  private String name;

  private String telephone;

  private String address;

}
