package org.seagulls.cainiao.common.listener.impl;


import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;
import org.seagulls.cainiao.R;
import org.seagulls.cainiao.identitycode.IdentityCodeActivity;
import org.seagulls.cainiao.parceldetails.ParcelDetailsActivity;
import org.seagulls.cainiao.parceldispatch.ParcelDispatchActivity;

/**
 * Home Fragment 中所有包含跳转逻辑的点击事件所使用的监听器，例如从首页跳转到包裹详情界面
 */
public class HomeFragmentOCL implements OnClickListener {

  /**
   * 发生点击事件的上下文环境
   */
  private final Context context;

  /**
   * Home Fragment 的“用户包裹列表”发生点击事件时提供的被点击的包裹 id 数据
   */
  private int id;

  public HomeFragmentOCL(Context context) {
    this.context = context;
  }

  public HomeFragmentOCL(Context context, int id) {
    this(context);
    this.id = id;
  }

  @Override
  public void onClick(View v) {
    // 首页 地区切换
    if (v.getId() == R.id.tv_location) {
      Toast.makeText(context, "暂不支持切换地区！", Toast.LENGTH_SHORT).show();
    }
    // 首页 取包裹
    else if (v.getId() == R.id.tv_parcel_pickup) {
      Toast.makeText(context, "取件", Toast.LENGTH_SHORT).show();
    }
    // 首页 寄包裹
    else if (v.getId() == R.id.tv_parcel_dispatch) {
      context.startActivity(new Intent(context, ParcelDispatchActivity.class));
    }
    // 首页 身份码
    else if (v.getId() == R.id.tv_identity_code) {
      context.startActivity(new Intent(context, IdentityCodeActivity.class));
    }
    // 首页 用户包裹列表的某个 Item
    else if (v.getId() == R.id.lv_item) {
      Intent intent = new Intent(context, ParcelDetailsActivity.class);
      intent.putExtra("parcelId", id);
      context.startActivity(intent);
    }
  }

}
