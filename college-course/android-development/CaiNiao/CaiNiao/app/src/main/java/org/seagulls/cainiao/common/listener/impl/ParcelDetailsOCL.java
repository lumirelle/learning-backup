package org.seagulls.cainiao.common.listener.impl;

import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;
import org.seagulls.cainiao.R;

/**
 * “包裹详情”界面中所有包含跳转逻辑的点击事件所使用的监听器
 */
public class ParcelDetailsOCL implements OnClickListener {

  /**
   * 点击事件发生的上下文环境
   */
  private final Context context;

  public ParcelDetailsOCL(Context context) {
    this.context = context;
  }

  @Override
  public void onClick(View v) {
    // 联系物流
    if (v.getId() == R.id.tv_contact_logistics) {
      Toast.makeText(context, "联系物流", Toast.LENGTH_SHORT).show();
    }
    // 联系客服
    else if (v.getId() == R.id.tv_contact_service) {
      Toast.makeText(context, "联系客服", Toast.LENGTH_SHORT).show();
    }
  }

}
