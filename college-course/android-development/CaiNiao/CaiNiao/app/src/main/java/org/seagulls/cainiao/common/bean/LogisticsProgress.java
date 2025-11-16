package org.seagulls.cainiao.common.bean;

import java.io.Serializable;
import lombok.Data;
import org.seagulls.cainiao.common.bean.type.LogisticsStatus;

/**
 * 物流进度数据类
 */
@Data
public class LogisticsProgress implements Serializable {

  private static final long serialVersionUID = 1L;

  private int id;

  private String datetime;

  private LogisticsStatus status;

  private String message;

}
