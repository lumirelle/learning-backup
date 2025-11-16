package org.seagulls.cainiao.main.home;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView.Adapter;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;
import com.bumptech.glide.Glide;
import java.util.List;
import org.seagulls.cainiao.common.bean.LogisticsProgress;
import org.seagulls.cainiao.common.bean.Parcel;
import org.seagulls.cainiao.common.listener.impl.HomeFragmentOCL;
import org.seagulls.cainiao.databinding.ItemParcelFragmentHomeBinding;

public class ParcelAdapter extends Adapter<ViewHolder> {

  private final List<Parcel> parcels;

  private Context context;

  private ItemParcelFragmentHomeBinding binding;

  public ParcelAdapter(List<Parcel> parcels) {
    this.parcels = parcels;
  }

  @NonNull
  @Override
  public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
      int viewType) {
    context = parent.getContext();
    binding = ItemParcelFragmentHomeBinding.inflate(
        LayoutInflater.from(context), parent, false);
    return new ParcelViewHolder(binding.getRoot());
  }

  @SuppressLint("SetTextI18n")
  @Override
  public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
    // 为 Item 和对应的包裹绑定点击事件
    binding.getRoot()
        .setOnClickListener(
            new HomeFragmentOCL(context, parcels.get(position).getId()));
    // 加载 Item 界面
    ParcelViewHolder parcelHolder = (ParcelViewHolder) holder;
    Parcel parcel = parcels.get(position);
    Glide.with(context)
        .load(parcel.getImageUrl())
        .into(parcelHolder.ivParcelImage);
    parcelHolder.tvLogisticsStatus.setText(
        parcel.getLogisticsStatus().toString());
    parcelHolder.tvParcelTitle.setText(parcel.getTitle());
    // 界面需要显示最新的物流进度，没有则创建一个空的占位物流进度
    LogisticsProgress logisticsProgress = parcel.getLatestLogisticsProgress();
    if (logisticsProgress == null) {
      logisticsProgress = new LogisticsProgress();
      logisticsProgress.setMessage("暂无物流信息！");
    }
    parcelHolder.tvLogisticsDetail.setText(
        parcel.getLogisticsCompany().toString() + " | "
            + logisticsProgress.getMessage());
  }


  @Override
  public int getItemCount() {
    return parcels.size();
  }

  class ParcelViewHolder extends ViewHolder {

    final ImageView ivParcelImage;

    final TextView tvLogisticsStatus;

    final TextView tvParcelTitle;

    final TextView tvLogisticsDetail;

    public ParcelViewHolder(@NonNull View itemView) {
      super(itemView);
      ivParcelImage = binding.ivParcelImageHomeFragment;
      tvLogisticsStatus = binding.tvLogisticsStatus;
      tvParcelTitle = binding.tvParcelTitle;
      tvLogisticsDetail = binding.tvLogisticsDetail;
    }

  }

}
