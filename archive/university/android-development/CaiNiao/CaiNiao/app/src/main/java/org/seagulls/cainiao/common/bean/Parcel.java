package org.seagulls.cainiao.common.bean;

import com.alibaba.fastjson2.annotation.JSONField;
import java.io.Serializable;
import java.util.List;
import lombok.Data;
import org.seagulls.cainiao.common.bean.type.LogisticsCompany;
import org.seagulls.cainiao.common.bean.type.LogisticsStatus;

/**
 * 包裹数据类
 */
@Data
public class Parcel implements Serializable {

  private static final long serialVersionUID = 1L;

  private int id;

  private String title;

  private String imageUrl;

  private String logisticsId;

  private LogisticsStatus logisticsStatus;

  private List<LogisticsProgress> logisticsProgresses;

  private LogisticsCompany logisticsCompany;

  private ParcelStakeholder receiver;

  private ParcelStakeholder sender;

  /**
   * 获取最新的包裹进度
   *
   * @return 最新的包裹进度，无则返回 null
   */
  @JSONField(serialize = false, deserialize = false)
  public LogisticsProgress getLatestLogisticsProgress() {
    if (logisticsProgresses.isEmpty()) {
      return null;
    } else {
      return logisticsProgresses.get(logisticsProgresses.size() - 1);
    }
  }

}
