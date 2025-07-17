package org.seagulls.cainiao.parceldetails;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View.OnClickListener;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import org.seagulls.cainiao.R;
import org.seagulls.cainiao.common.bean.Parcel;
import org.seagulls.cainiao.common.bean.type.LogisticsCompany;
import org.seagulls.cainiao.common.listener.impl.ParcelDetailsOCL;
import org.seagulls.cainiao.common.server.ServerRequester;
import org.seagulls.cainiao.common.util.LogTagGenerator;
import org.seagulls.cainiao.databinding.ActivityParcelDetailsBinding;

public class ParcelDetailsActivity extends AppCompatActivity {

  private static final String LOG_TAG = LogTagGenerator.generate(
      ParcelDetailsActivity.class);

  private ActivityParcelDetailsBinding binding;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Log.d(LOG_TAG, "启动包裹详情界面");
    // ActivityParcelDetailsBinding 相当于 activity_parcel_details.xml 文件
    binding = ActivityParcelDetailsBinding.inflate(getLayoutInflater());
    // binding.getRoot() 获得的就是 activity_parcel_details.xml 里的最外层布局
    setContentView(binding.getRoot());
    initViewsWithData();
    setListeners();
  }

  @SuppressLint("SetTextI18n")
  private void initViewsWithData() {
    Log.d(LOG_TAG, "加载数据并初始化界面");

    int id = getIntent().getIntExtra("parcelId", -1);
    Log.d(LOG_TAG, "数据：包裹 ID = " + id + "，来源 = HomeFragment");

    ServerRequester serverRequester = new ServerRequester(this);
    serverRequester.queryParcelById(id, parcel -> runOnUiThread(() -> {
      Log.d(LOG_TAG, "数据：包裹 = " + parcel + "，来源 = 服务器");
      initViews(parcel);
    }));
  }

  private void initViews(Parcel parcel) {
    Log.d(LOG_TAG, "开始初始化界面");
    // 加载包裹图片
    Glide.with(this)
        .load(parcel.getImageUrl())
        .into(binding.ivParcelImageParcelDetails);
    // 加载物流公司图片
    if (parcel.getLogisticsCompany() == LogisticsCompany.SHEN_TONG) {
      binding.ivLogisticsImage.setImageResource(
          R.drawable.ic_logistics_company_sto);
    } else if (parcel.getLogisticsCompany() == LogisticsCompany.YUAN_TONG) {
      binding.ivLogisticsImage.setImageResource(
          R.drawable.ic_logistics_company_yto);
    } else if (parcel.getLogisticsCompany() == LogisticsCompany.ZHONG_TONG) {
      binding.ivLogisticsImage.setImageResource(
          R.drawable.ic_logistics_company_zto);
    } else if (parcel.getLogisticsCompany() == LogisticsCompany.DEPPON) {
      binding.ivLogisticsImage.setImageResource(
          R.drawable.ic_logistics_company_deppon);
    } else if (parcel.getLogisticsCompany() == LogisticsCompany.YUN_DA) {
      binding.ivLogisticsImage.setImageResource(
          R.drawable.ic_logistics_company_yunda);
    } else if (parcel.getLogisticsCompany() == LogisticsCompany.CHINA_POST) {
      binding.ivLogisticsImage.setImageResource(
          R.drawable.ic_logistics_company_china_post);
    } else if (parcel.getLogisticsCompany() == LogisticsCompany.SHUN_FENG) {
      binding.ivLogisticsImage.setImageResource(
          R.drawable.ic_logistics_company_sf);
    } else if (parcel.getLogisticsCompany() == LogisticsCompany.JI_TU) {
      binding.ivLogisticsImage.setImageResource(
          R.drawable.ic_logistics_company_jt);
    }
    // 加载物流公司名称
    binding.tvLogisticsCompany.setText(parcel.getLogisticsCompany().toString());
    // 加载物流 ID
    binding.tvLogisticsId.setText(parcel.getLogisticsId());
    // 加载物流进度（RecyclerView）
    RecyclerView recyclerView = binding.rvLogisticsProgresses;
    recyclerView.setLayoutManager(new LinearLayoutManager(this));
    recyclerView.setAdapter(
        new ParcelDetailsAdapter(parcel.getSender(), parcel.getReceiver(),
            parcel.getLogisticsProgresses()));
  }

  private void setListeners() {
    Log.d(LOG_TAG, "设置监听器");

    OnClickListener listenerGoTo = new ParcelDetailsOCL(this);
    binding.tvContactLogistics.setOnClickListener(listenerGoTo);
    binding.tvContactService.setOnClickListener(listenerGoTo);

    OnClickListener listener = view -> {
      // 返回
      if (view.getId() == R.id.tv_back) {
        this.finish();
      }
      // 复制
      else if (view.getId() == R.id.btn_copy_logistics_id) {
        String id = binding.tvLogisticsId.getText().toString();
        ClipboardManager clipboardManager = (ClipboardManager) getSystemService(
            Context.CLIPBOARD_SERVICE);
        ClipData clipData = ClipData.newPlainText("菜鸟驿站：包裹物流 ID", id);
        clipboardManager.setPrimaryClip(clipData);
        Toast.makeText(this, "已复制物流 ID", Toast.LENGTH_SHORT).show();
      }
      // 分享
      else if (view.getId() == R.id.btn_share_logistics_id) {
        String id = binding.tvLogisticsId.getText().toString();
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_TEXT, id);
        intent.setType("text/plain");
        this.startActivity(Intent.createChooser(intent, "分享到……"));
      }
    };
    binding.tvBack.setOnClickListener(listener);
    binding.btnCopyLogisticsId.setOnClickListener(listener);
    binding.btnShareLogisticsId.setOnClickListener(listener);
  }

}
