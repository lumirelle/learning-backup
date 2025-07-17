package org.seagulls.cainiao.parceldetails;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView.Adapter;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;
import java.util.List;
import org.seagulls.cainiao.common.bean.LogisticsProgress;
import org.seagulls.cainiao.common.bean.ParcelStakeholder;
import org.seagulls.cainiao.databinding.ItemLogisticsProgressBinding;
import org.seagulls.cainiao.databinding.ItemLogisticsProgressReceiverBinding;
import org.seagulls.cainiao.databinding.ItemLogisticsProgressSenderBinding;

public class ParcelDetailsAdapter extends Adapter<ViewHolder> {

  private final ParcelStakeholder sender;

  private final ParcelStakeholder receiver;

  private final List<LogisticsProgress> logisticsProgresses;

  /**
   * 视图类型，包括：寄件人、收件人、物流进度三种
   */
  private static final int VIEW_TYPE_SENDER = 1;

  /**
   * 视图类型，包括：寄件人、收件人、物流进度三种
   */
  private static final int VIEW_TYPE_RECEIVER = 2;

  /**
   * 视图类型，包括：寄件人、收件人、物流进度三种
   */
  private static final int VIEW_TYPE_PROGRESS = 3;

  private ItemLogisticsProgressSenderBinding senderBinding;

  private ItemLogisticsProgressReceiverBinding receiverBinding;

  private ItemLogisticsProgressBinding progressBinding;

  public ParcelDetailsAdapter(ParcelStakeholder sender,
      ParcelStakeholder receiver, List<LogisticsProgress> logisticsProgresses) {
    this.sender = sender;
    this.receiver = receiver;
    this.logisticsProgresses = logisticsProgresses;
  }

  @NonNull
  @Override
  public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
      int viewType) {
    Context context = parent.getContext();
    senderBinding = ItemLogisticsProgressSenderBinding.inflate(
        LayoutInflater.from(context), parent, false);
    receiverBinding = ItemLogisticsProgressReceiverBinding.inflate(
        LayoutInflater.from(context), parent, false);
    progressBinding = ItemLogisticsProgressBinding.inflate(
        LayoutInflater.from(context), parent, false);

    if (viewType == VIEW_TYPE_SENDER) {
      return new SenderViewHolder(senderBinding.getRoot());
    } else if (viewType == VIEW_TYPE_RECEIVER) {
      return new ReceiverViewHolder(receiverBinding.getRoot());
    } else if (viewType == VIEW_TYPE_PROGRESS) {
      return new ProgressViewHolder(progressBinding.getRoot());
    } else {
      throw new UnsupportedOperationException("不支持的 viewType。");
    }
  }

  @Override
  public int getItemViewType(int position) {
    if (position == 0) {
      return VIEW_TYPE_SENDER;
    } else if (position == getItemCount() - 1) {
      return VIEW_TYPE_RECEIVER;
    } else {
      return VIEW_TYPE_PROGRESS;
    }
  }

  @SuppressLint("SetTextI18n")
  @Override
  public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
    // 寄件人
    if (holder instanceof SenderViewHolder) {
      SenderViewHolder senderHolder = (SenderViewHolder) holder;
      senderHolder.tvSenderAddress.setText("来自 " + sender.getAddress());
      // 除了姓都替换成 *，保密
      senderHolder.tvSenderDetails.setText(
          sender.getName().charAt(0) + "** " + sender.getTelephone());
    }
    // 收件人
    else if (holder instanceof ReceiverViewHolder) {
      ReceiverViewHolder receiverHolder = (ReceiverViewHolder) holder;
      receiverHolder.tvReceiverAddress.setText("送至 " + receiver.getAddress());
      // 除了姓都替换成 *，保密
      receiverHolder.tvReceiverDetails.setText(
          receiver.getName().charAt(0) + "** " + receiver.getTelephone());
    }
    // 物流进度
    else if (holder instanceof ProgressViewHolder) {
      ProgressViewHolder progressHolder = (ProgressViewHolder) holder;
      progressHolder.tvProgressDatetime.setText(
          logisticsProgresses.get(position - 1).getDatetime());
      if (logisticsProgresses.get(position - 1).getStatus() == null) {
        progressHolder.tvProgressStatus.setText("");
      } else {
        progressHolder.tvProgressStatus.setText(
            " I " + logisticsProgresses.get(position - 1)
                .getStatus()
                .toString());
      }
      progressHolder.tvProgressMessage.setText(
          logisticsProgresses.get(position - 1).getMessage());
    }
  }

  @Override
  public int getItemCount() {
    return logisticsProgresses.size() + 2;
  }

  class SenderViewHolder extends ViewHolder {

    final TextView tvSenderAddress;

    final TextView tvSenderDetails;

    public SenderViewHolder(@NonNull View itemView) {
      super(itemView);
      tvSenderAddress = senderBinding.tvSenderAddress;
      tvSenderDetails = senderBinding.tvSenderDetails;
    }

  }

  class ReceiverViewHolder extends ViewHolder {

    final TextView tvReceiverAddress;

    final TextView tvReceiverDetails;

    public ReceiverViewHolder(@NonNull View itemView) {
      super(itemView);
      tvReceiverAddress = receiverBinding.tvReceiverAddress;
      tvReceiverDetails = receiverBinding.tvReceiverDetails;
    }

  }

  class ProgressViewHolder extends ViewHolder {

    final TextView tvProgressDatetime;

    final TextView tvProgressStatus;

    final TextView tvProgressMessage;

    public ProgressViewHolder(@NonNull View itemView) {
      super(itemView);
      tvProgressDatetime = progressBinding.tvProgressDatetime;
      tvProgressStatus = progressBinding.tvProgressStatus;
      tvProgressMessage = progressBinding.tvProgressMessage;
    }

  }

}
